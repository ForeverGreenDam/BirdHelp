package com.greendam.birdhelp.internal;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.ChatMessageMapper;
import com.greendam.birdhelp.mapper.FileRecordMapper;
import com.greendam.birdhelp.model.entity.ChatMessage;
import com.greendam.birdhelp.model.entity.FileRecord;
import com.greendam.birdhelp.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 对话修改内部 API 控制器，供 Python AI 模块调用（RSA 签名校验保护）。
 * </p>
 *
 * <h3>接口清单</h3>
 * <table>
 *   <tr><th>方法</th><th>路径</th><th>用途</th><th>Python 调用</th></tr>
 *   <tr><td>GET</td><td>/internal/file/{id}/outline</td><td>读取大纲</td><td>✅</td></tr>
 *   <tr><td>PUT</td><td>/internal/file/{id}/outline</td><td>更新大纲</td><td>✅</td></tr>
 *   <tr><td>POST</td><td>/internal/chat/session</td><td>获取或创建会话</td><td>✅</td></tr>
 *   <tr><td>POST</td><td>/internal/chat/session/{id}/messages</td><td>追加消息</td><td>✅</td></tr>
 *   <tr><td>DELETE</td><td>/internal/chat/session/{id}</td><td>删除会话</td><td>❌</td></tr>
 * </table>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/internal")
public class ChatInternalController {

    @Resource
    private FileRecordMapper fileRecordMapper;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    // ==================== 大纲读写 ====================

    /**
     * 读取文件的文档大纲 JSON。Python 对话修改时调用，用于获取 100% 保真的大纲。
     *
     * @param id 文件记录 ID
     * @return 大纲 JSON 字符串（可能为 {@code null}）
     */
    @GetMapping("/file/{id}/outline")
    public BaseResponse<Map<String, Object>> getOutline(@PathVariable Long id) {
        FileRecord record = fileRecordMapper.selectById(id);
        if (record == null) {
            return BaseResponse.error(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("fileId", id);
        data.put("outline", record.getOutline());
        data.put("fileName", record.getFileName());
        data.put("fileType", record.getFileType());
        log.info("大纲读取: fileId={}, hasOutline={}", id, record.getOutline() != null);
        return BaseResponse.success(data);
    }

    /**
     * 更新文件的文档大纲 JSON。Python 对话修改完成后调用，将修改后的大纲写回 DB。
     *
     * @param id   文件记录 ID
     * @param body 请求体，包含 {@code outline} 字段
     * @return 更新结果
     */
    @PutMapping("/file/{id}/outline")
    public BaseResponse<Void> updateOutline(@PathVariable Long id, @RequestBody Map<String, String> body) {
        FileRecord record = fileRecordMapper.selectById(id);
        if (record == null) {
            return BaseResponse.error(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }
        String outline = body.get("outline");
        record.setOutline(outline);
        fileRecordMapper.updateById(record);
        log.info("大纲更新: fileId={}, outlineLength={}", id, outline != null ? outline.length() : 0);
        return BaseResponse.success();
    }

    // ==================== 会话管理 ====================

    /**
     * 获取或创建对话会话（幂等）。同一 sessionId 多次调用只创建一次。
     *
     * <h3>请求体</h3>
     * <pre>{@code
     * {
     *     "sessionId": "uuid-v4",
     *     "userId": 1,
     *     "projectId": 1,
     *     "originalFileId": 100,
     *     "docType": "ppt"
     * }
     * }</pre>
     *
     * @param body 请求体
     * @return 会话信息，包含历史消息列表
     */
    @PostMapping("/chat/session")
    public BaseResponse<Map<String, Object>> getOrCreateSession(@RequestBody Map<String, Object> body) {
        String sessionId = (String) body.get("sessionId");
        Long userId = toLong(body.get("userId"));
        Long projectId = toLong(body.get("projectId"));
        Long originalFileId = toLong(body.get("originalFileId"));
        String docType = (String) body.get("docType");

        if (sessionId == null || userId == null || projectId == null
                || originalFileId == null || docType == null) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR, "参数不完整: sessionId/userId/projectId/originalFileId/docType 均为必填");
        }

        chatSessionService.getOrCreateSession(sessionId, userId, projectId, originalFileId, docType);
        List<ChatMessage> messages = chatSessionService.getMessages(sessionId);

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("originalFileId", originalFileId);
        data.put("currentFileId", originalFileId); // 新会话的 currentFileId = originalFileId
        data.put("docType", docType);
        data.put("messages", messages);

        log.info("获取/创建会话: sessionId={}, userId={}, docType={}, messageCount={}",
                sessionId, userId, docType, messages.size());
        return BaseResponse.success(data);
    }

    /**
     * 追加消息到会话。Python 每次对话轮次结束后调用，同时追加 user 和 assistant 两方消息。
     *
     * <h3>请求体</h3>
     * <pre>{@code
     * {
     *     "messages": [
     *         {"role": "user", "content": "把第二页标题改得激进一些"},
     *         {"role": "assistant", "content": "已修改...", "fileId": 101}
     *     ],
     *     "currentFileId": 101
     * }
     * }</pre>
     *
     * @param sessionId 会话 ID（路径参数）
     * @param body      请求体
     * @return 追加结果
     */
    @PostMapping("/chat/session/{sessionId}/messages")
    public BaseResponse<Map<String, Object>> appendMessages(@PathVariable String sessionId,
                                                            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        Object currentFileIdObj = body.get("currentFileId");

        if (messages == null || messages.isEmpty()) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR, "messages 不能为空");
        }

        int appendedCount = 0;
        for (Map<String, Object> msg : messages) {
            String role = (String) msg.get("role");
            String content = (String) msg.get("content");
            Long fileId = toLong(msg.get("fileId"));

            chatSessionService.appendMessage(sessionId, role, content, fileId);
            appendedCount++;
        }

        // 更新当前最新版本文件
        if (currentFileIdObj != null) {
            Long currentFileId = toLong(currentFileIdObj);
            if (currentFileId != null) {
                chatSessionService.updateCurrentFile(sessionId, currentFileId);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("appendedCount", appendedCount);

        log.info("消息批量追加: sessionId={}, count={}", sessionId, appendedCount);
        return BaseResponse.success(data);
    }

    // ==================== 辅助方法 ====================

    /**
     * 安全地将 Object 转为 Long，支持 Integer 和 Number 类型。
     */
    private Long toLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
