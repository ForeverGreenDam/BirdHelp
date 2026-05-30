package com.greendam.birdhelp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.common.utils.AiModuleCaller;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.model.dto.ChatDiscussDTO;
import com.greendam.birdhelp.model.dto.ChatModifyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private ObjectMapper objectMapper;

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
