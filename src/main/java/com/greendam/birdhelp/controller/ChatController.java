package com.greendam.birdhelp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.common.utils.AiModuleCaller;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.model.dto.ChatDiscussDTO;
import com.greendam.birdhelp.model.dto.ChatModifyDTO;
import com.greendam.birdhelp.model.dto.CreateSessionDTO;
import com.greendam.birdhelp.model.entity.ChatSession;
import com.greendam.birdhelp.model.vo.ChatSessionDetailVO;
import com.greendam.birdhelp.model.vo.ChatSessionVO;
import com.greendam.birdhelp.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 对话修改模块接口控制器，接收前端请求后代理转发至 Python AI 模块。
 * </p>
 *
 * <h3>数据流</h3>
 * <pre>
 * 前端 → POST /chat/modify → ChatController (JWT 鉴权 + 日志)
 *      → AiModuleCaller (RSA 签名) → Python POST /ai/chat/modify
 *      → Python 修改模块 (LLM + 文件重建)
 *      → 返回响应 → 前端
 * </pre>
 *
 * <h3>鉴权说明</h3>
 * <p>所有接口需携带有效 JWT Token，通过 {@code WebMvcConfiguration} 中注册的
 * {@code /chat/**} 拦截路径进行校验。</p>
 *
 * @author ForeverGreenDam
 * @see AiModuleCaller#chatModify(String)
 * @see AiModuleCaller#chatDiscuss(String)
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private AiModuleCaller aiModuleCaller;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private ObjectMapper objectMapper;

    // ==================== 会话管理（纯 Java，不调用 Python） ====================

    /**
     * 创建新会话。用户选中文件后调用，Java 生成 sessionId 并入库，返回给前端用于后续对话。
     *
     * <h3>请求体示例</h3>
     * <pre>{@code
     * {
     *     "fileId": "100",
     *     "docType": "ppt",
     *     "projectId": 1,
     *     "title": "自定义标题（可选）"
     * }
     * }</pre>
     *
     * @param dto 含 fileId / docType / projectId / title(可选)
     * @return 新会话的 sessionId 和 title
     */
    @PostMapping("/session")
    public BaseResponse<Map<String, Object>> createSession(@RequestBody CreateSessionDTO dto) {
        Long userId = BaseContext.getCurrentId();

        if (dto.getFileId() == null || dto.getDocType() == null || dto.getProjectId() == null) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR, "缺少必填参数: fileId/docType/projectId");
        }

        String sessionId = java.util.UUID.randomUUID().toString();
        Long originalFileId = Long.parseLong(dto.getFileId());

        ChatSession session = chatSessionService.getOrCreateSession(
                sessionId, userId, dto.getProjectId(), originalFileId, dto.getDocType(), dto.getTitle());

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", session.getSessionId());
        data.put("title", session.getTitle());

        log.info("会话已创建: sessionId={}, title={}, fileId={}, docType={}, userId={}",
                sessionId, session.getTitle(), dto.getFileId(), dto.getDocType(), userId);
        return BaseResponse.success(data);
    }

    /**
     * 获取当前用户的所有会话列表（全局左侧栏）。
     * <p>按最后更新时间倒序排列，不含消息内容，仅返回摘要信息（标题、文件类型、消息数、最后一条消息预览）。</p>
     *
     * @return 会话视图列表
     */
    @GetMapping("/sessions")
    public BaseResponse<List<ChatSessionVO>> listSessions() {
        Long userId = BaseContext.getCurrentId();
        List<ChatSessionVO> sessions = chatSessionService.listUserSessions(userId);
        log.info("会话列表查询: userId={}, count={}", userId, sessions.size());
        return BaseResponse.success(sessions);
    }

    /**
     * 获取会话详情（含完整历史消息），用于点击左侧标签后恢复会话。
     * <p>返回会话元信息 + 所有消息（按时间正序）。前端遍历 messages，
     * 收集 assistant 消息中非空的 {@code fileId} 构建版本时间线。</p>
     *
     * @param sessionId 会话 ID（路径参数）
     * @return 会话详情（含 messages 数组）
     */
    @GetMapping("/session/{sessionId}")
    public BaseResponse<ChatSessionDetailVO> getSessionDetail(@PathVariable String sessionId) {
        ChatSessionDetailVO detail = chatSessionService.getSessionDetail(sessionId);
        log.info("会话详情查询: sessionId={}, messageCount={}",
                sessionId, detail.getMessages() != null ? detail.getMessages().size() : 0);
        return BaseResponse.success(detail);
    }

    /**
     * 删除会话（软删除，不涉及文件）。
     * <p>仅将 {@code chat_session.del_flag} 置为 1，消息记录保留不删，
     * 该会话涉及的所有文件不受影响。</p>
     *
     * @param sessionId 会话 ID（路径参数）
     * @return 空响应
     */
    @DeleteMapping("/session/{sessionId}")
    public BaseResponse<Void> deleteSession(@PathVariable String sessionId) {
        chatSessionService.deleteSession(sessionId);
        log.info("会话已删除: sessionId={}", sessionId);
        return BaseResponse.success();
    }

    // ==================== 对话代理（调用 Python） ====================

    /**
     * 对话修改文档。接收用户修改指令，代理转发至 Python AI 模块执行 LLM 修改 + 文件重建。
     *
     * <h3>请求体示例</h3>
     * <pre>{@code
     * {
     *     "sessionId": "uuid-v4",
     *     "fileId": "100",
     *     "docType": "ppt",
     *     "message": "把第二页标题改得激进一些",
     *     "projectId": 1,
     *     "history": [{"role": "user", "content": "..."}],
     *     "regenerateFile": true
     * }
     * }</pre>
     *
     * @param dto 前端请求体，含 sessionId/fileId/docType/message/projectId/history/regenerateFile
     * @return Python 响应的统一包装（含 AI 回复、新大纲、变更列表、新文件 ID/URL）
     */
    @PostMapping("/modify")
    public BaseResponse<Map<String, Object>> modify(@RequestBody ChatModifyDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("收到对话修改请求: userId={}, sessionId={}, fileId={}, docType={}, messageLen={}, regenerateFile={}",
                userId, dto.getSessionId(), dto.getFileId(), dto.getDocType(),
                dto.getMessage() != null ? dto.getMessage().length() : 0,
                dto.getRegenerateFile());

        // 参数校验
        if (dto.getSessionId() == null || dto.getFileId() == null
                || dto.getDocType() == null || dto.getMessage() == null || dto.getProjectId() == null) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR,
                    "缺少必填参数: sessionId/fileId/docType/message/projectId");
        }

        try {
            // 构建发给 Python 的请求体，注入 userId（从 JWT 获取而非前端传入）
            Map<String, Object> pythonBody = new HashMap<>();
            pythonBody.put("userId", String.valueOf(userId));
            pythonBody.put("projectId", String.valueOf(dto.getProjectId()));
            pythonBody.put("sessionId", dto.getSessionId());
            pythonBody.put("fileId", dto.getFileId());
            pythonBody.put("docType", dto.getDocType());
            pythonBody.put("message", dto.getMessage());
            pythonBody.put("history", dto.getHistory() != null ? dto.getHistory() : List.of());
            pythonBody.put("regenerateFile",
                    dto.getRegenerateFile() != null ? dto.getRegenerateFile() : true);
            pythonBody.put("callbackId", "");

            String requestJson = objectMapper.writeValueAsString(pythonBody);
            String responseJson = aiModuleCaller.chatModify(requestJson);

            @SuppressWarnings("unchecked")
            Map<String, Object> pythonResp = objectMapper.readValue(responseJson, Map.class);

            log.info("对话修改完成: userId={}, sessionId={}, pythonCode={}",
                    userId, dto.getSessionId(), pythonResp.get("code"));

            @SuppressWarnings("unchecked")
            Map<String, Object> modifyData = (Map<String, Object>) pythonResp.get("data");

            // Python 首轮对话后返回 LLM 生成的标题，回填到 chat_session
            if (modifyData != null && modifyData.get("title") instanceof String) {
                String generatedTitle = (String) modifyData.get("title");
                if (!generatedTitle.isBlank()) {
                    chatSessionService.updateTitle(dto.getSessionId(), generatedTitle);
                    // 前端拿到最新标题后可即时更新左侧栏标签
                    modifyData.put("title", generatedTitle);
                }
            }

            return BaseResponse.success(modifyData);

        } catch (Exception e) {
            log.error("对话修改代理失败: userId={}, sessionId={}", userId, dto.getSessionId(), e);
            return BaseResponse.error(ErrorCode.SYSTEM_ERROR, "对话修改服务异常: " + e.getMessage());
        }
    }

    /**
     * 对话讨论/仅问答。接收用户消息，代理转发至 Python AI 模块执行 LLM 问答（不重建文件）。
     *
     * <h3>请求体示例</h3>
     * <pre>{@code
     * {
     *     "sessionId": "uuid-v4",
     *     "fileId": "100",
     *     "docType": "ppt",
     *     "message": "这个文档的第一页标题有什么问题？",
     *     "projectId": 1,
     *     "history": [{"role": "user", "content": "..."}]
     * }
     * }</pre>
     *
     * @param dto 前端请求体，含 sessionId/fileId/docType/message/projectId/history
     * @return Python 响应的统一包装（含 AI 回复文本，fileId/fileUrl 为空）
     */
    @PostMapping("/discuss")
    public BaseResponse<Map<String, Object>> discuss(@RequestBody ChatDiscussDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("收到对话讨论请求: userId={}, sessionId={}, fileId={}, docType={}, messageLen={}",
                userId, dto.getSessionId(), dto.getFileId(), dto.getDocType(),
                dto.getMessage() != null ? dto.getMessage().length() : 0);

        // 参数校验
        if (dto.getSessionId() == null || dto.getFileId() == null
                || dto.getDocType() == null || dto.getMessage() == null || dto.getProjectId() == null) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR,
                    "缺少必填参数: sessionId/fileId/docType/message/projectId");
        }

        try {
            // 构建发给 Python 的请求体，注入 userId
            Map<String, Object> pythonBody = new HashMap<>();
            pythonBody.put("userId", String.valueOf(userId));
            pythonBody.put("projectId", String.valueOf(dto.getProjectId()));
            pythonBody.put("sessionId", dto.getSessionId());
            pythonBody.put("fileId", dto.getFileId());
            pythonBody.put("docType", dto.getDocType());
            pythonBody.put("message", dto.getMessage());
            pythonBody.put("history", dto.getHistory() != null ? dto.getHistory() : List.of());
            pythonBody.put("regenerateFile", false);  // 讨论不重建文件
            pythonBody.put("callbackId", "");

            String requestJson = objectMapper.writeValueAsString(pythonBody);
            String responseJson = aiModuleCaller.chatDiscuss(requestJson);

            @SuppressWarnings("unchecked")
            Map<String, Object> pythonResp = objectMapper.readValue(responseJson, Map.class);

            log.info("对话讨论完成: userId={}, sessionId={}, pythonCode={}",
                    userId, dto.getSessionId(), pythonResp.get("code"));

            @SuppressWarnings("unchecked")
            Map<String, Object> discussData = (Map<String, Object>) pythonResp.get("data");
            return BaseResponse.success(discussData);

        } catch (Exception e) {
            log.error("对话讨论代理失败: userId={}, sessionId={}", userId, dto.getSessionId(), e);
            return BaseResponse.error(ErrorCode.SYSTEM_ERROR, "对话讨论服务异常: " + e.getMessage());
        }
    }
}
