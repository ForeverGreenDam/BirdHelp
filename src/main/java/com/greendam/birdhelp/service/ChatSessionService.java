package com.greendam.birdhelp.service;

import com.greendam.birdhelp.model.entity.ChatMessage;
import com.greendam.birdhelp.model.entity.ChatSession;
import com.greendam.birdhelp.model.vo.ChatSessionDetailVO;
import com.greendam.birdhelp.model.vo.ChatSessionVO;

import java.util.List;

/**
 * <p>
 * 对话修改会话服务接口。
 * </p>
 *
 * <h3>会话生命周期</h3>
 * <ol>
 *   <li>用户新建对话、选文件 → 前端生成 sessionId → Python 调用 {@code getOrCreateSession} 创建会话</li>
 *   <li>每次对话轮次 → Python 调用 {@code appendMessage} 追加用户消息和 AI 回复</li>
 *   <li>每次修改后 → Python 调用 {@code updateCurrentFile} 更新当前最新版本文件</li>
 *   <li>前端左侧栏加载 → {@code GET /chat/sessions} → {@code listUserSessions}</li>
 *   <li>用户点击标签 → {@code GET /chat/session/{id}} → {@code getSessionDetail}</li>
 * </ol>
 *
 * @author ForeverGreenDam
 */
public interface ChatSessionService {

    /**
     * 获取或创建会话（幂等）。按 sessionId 查找，已存在则直接返回，不存在才创建。
     *
     * @param sessionId      会话 ID（UUID v4）
     * @param userId         用户 ID
     * @param projectId      项目 ID
     * @param originalFileId 修改的起点文件 ID
     * @param docType        文档类型（ppt / word / pdf）
     * @param title          会话标题（可选），{@code null} 或空时取原始文件名去扩展名
     * @return 会话实体（已存在则返回已有，不存在则创建新会话）
     */
    ChatSession getOrCreateSession(String sessionId, Long userId, Long projectId,
                                   Long originalFileId, String docType, String title);

    /**
     * 查询用户的所有会话列表（全局左侧栏），按更新时间倒序。
     *
     * @param userId 用户 ID
     * @return 会话视图列表（不含消息内容，仅摘要）
     */
    List<ChatSessionVO> listUserSessions(Long userId);

    /**
     * 查询会话详情（含完整历史消息），用于点击标签后恢复会话。
     *
     * @param sessionId 会话 ID
     * @return 会话详情（含元信息 + 完整消息列表）
     */
    ChatSessionDetailVO getSessionDetail(String sessionId);

    /**
     * 追加消息到会话。同时更新 {@code chat_session.message_count}。
     *
     * @param sessionId 会话 ID
     * @param role      消息角色（"user" 或 "assistant"）
     * @param content   消息内容
     * @param fileId    关联的文件 ID（assistant 消息可选，记录该轮产出的文件版本）
     * @return 追加的消息实体
     */
    ChatMessage appendMessage(String sessionId, String role, String content, Long fileId);

    /**
     * 查询会话的所有历史消息，按时间正序排列。
     *
     * @param sessionId 会话 ID
     * @return 消息列表（按 create_time ASC）
     */
    List<ChatMessage> getMessages(String sessionId);

    /**
     * 更新会话的当前最新版本文件 ID（每次修改后调用）。
     *
     * @param sessionId     会话 ID
     * @param currentFileId 最新生成的文件 ID
     */
    void updateCurrentFile(String sessionId, Long currentFileId);

    /**
     * 更新会话标题（LLM 生成标题后回填）。
     *
     * @param sessionId 会话 ID
     * @param title     新标题
     */
    void updateTitle(String sessionId, String title);

    /**
     * 删除会话及其所有消息（软删除 chat_session，消息保留）。
     *
     * @param sessionId 会话 ID
     */
    void deleteSession(String sessionId);
}
