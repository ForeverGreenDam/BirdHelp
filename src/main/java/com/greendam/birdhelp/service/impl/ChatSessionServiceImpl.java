package com.greendam.birdhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.ChatMessageMapper;
import com.greendam.birdhelp.mapper.ChatSessionMapper;
import com.greendam.birdhelp.model.entity.ChatMessage;
import com.greendam.birdhelp.model.entity.ChatSession;
import com.greendam.birdhelp.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 对话修改会话服务实现类。
 * </p>
 *
 * <h3>幂等性</h3>
 * <p>{@code getOrCreateSession} 按 {@code sessionId} 查找，已存在则直接返回，不存在才创建。
 * 避免 Python 重复调用时产生重复记录。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
        implements ChatSessionService {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional
    public ChatSession getOrCreateSession(String sessionId, Long userId, Long projectId,
                                          Long originalFileId, String docType) {
        // 按 session_id 查找已有会话
        ChatSession existing = getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId));

        if (existing != null) {
            log.info("会话已存在: sessionId={}", sessionId);
            return existing;
        }

        // 创建新会话
        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setProjectId(projectId);
        session.setOriginalFileId(originalFileId);
        session.setCurrentFileId(originalFileId); // 初始值 = 起点文件
        session.setDocType(docType);
        session.setMessageCount(0);
        save(session);

        log.info("会话已创建: sessionId={}, userId={}, originalFileId={}, docType={}",
                sessionId, userId, originalFileId, docType);
        return session;
    }

    @Override
    @Transactional
    public ChatMessage appendMessage(String sessionId, String role, String content, Long fileId) {
        // 校验会话存在
        ChatSession session = getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId));
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在: " + sessionId);
        }

        // 插入消息
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setFileId(fileId);
        chatMessageMapper.insert(message);

        // 更新消息计数
        session.setMessageCount(session.getMessageCount() + 1);
        session.setUpdateTime(LocalDateTime.now());
        updateById(session);

        log.info("消息已追加: sessionId={}, role={}, contentLength={}, fileId={}",
                sessionId, role, content != null ? content.length() : 0, fileId);
        return message;
    }

    @Override
    public List<ChatMessage> getMessages(String sessionId) {
        return chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreateTime));
    }

    @Override
    public void updateCurrentFile(String sessionId, Long currentFileId) {
        LambdaUpdateWrapper<ChatSession> wrapper = new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId)
                .set(ChatSession::getCurrentFileId, currentFileId)
                .set(ChatSession::getUpdateTime, LocalDateTime.now());

        update(wrapper);
        log.info("会话当前文件已更新: sessionId={}, currentFileId={}", sessionId, currentFileId);
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        LambdaUpdateWrapper<ChatSession> wrapper = new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId)
                .set(ChatSession::getDelFlag, 1);

        update(wrapper);
        log.info("会话已软删除: sessionId={}", sessionId);
    }
}
