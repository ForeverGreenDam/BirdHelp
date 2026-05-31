package com.greendam.birdhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.ChatMessageMapper;
import com.greendam.birdhelp.mapper.ChatSessionMapper;
import com.greendam.birdhelp.mapper.FileRecordMapper;
import com.greendam.birdhelp.model.entity.ChatMessage;
import com.greendam.birdhelp.model.entity.ChatSession;
import com.greendam.birdhelp.model.entity.FileRecord;
import com.greendam.birdhelp.model.vo.ChatSessionDetailVO;
import com.greendam.birdhelp.model.vo.ChatSessionVO;
import com.greendam.birdhelp.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    private FileRecordMapper fileRecordMapper;

    @Override
    @Transactional
    public ChatSession getOrCreateSession(String sessionId, Long userId, Long projectId,
                                          Long originalFileId, String docType, String title) {
        // 按 session_id 查找已有会话
        ChatSession existing = getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId));

        if (existing != null) {
            log.info("会话已存在: sessionId={}", sessionId);
            return existing;
        }

        // 标题：优先用传入值，否则取原始文件名去扩展名
        String finalTitle = (title != null && !title.isBlank()) ? title.trim() : "";
        if (finalTitle.isEmpty()) {
            FileRecord originalFile = fileRecordMapper.selectById(originalFileId);
            if (originalFile != null) {
                finalTitle = originalFile.getFileName();
                int dotIdx = finalTitle.lastIndexOf('.');
                if (dotIdx > 0) {
                    finalTitle = finalTitle.substring(0, dotIdx);
                }
            }
        }

        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setProjectId(projectId);
        session.setOriginalFileId(originalFileId);
        session.setCurrentFileId(originalFileId); // 初始值 = 起点文件
        session.setDocType(docType);
        session.setTitle(finalTitle);
        session.setMessageCount(0);
        save(session);

        log.info("会话已创建: sessionId={}, userId={}, originalFileId={}, title={}, docType={}",
                sessionId, userId, originalFileId, finalTitle, docType);
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

        // 更新消息计数（updateTime 由 MyMetaObjectHandler 自动填充）
        session.setMessageCount(session.getMessageCount() + 1);
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
        ChatSession session = getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId));
        if (session == null) {
            log.warn("更新 currentFileId 失败，会话不存在: sessionId={}", sessionId);
            return;
        }
        session.setCurrentFileId(currentFileId);
        updateById(session);  // 触发 MyMetaObjectHandler 自动填充 updateTime/updateBy
        log.info("会话当前文件已更新: sessionId={}, currentFileId={}", sessionId, currentFileId);
    }

    @Override
    public void updateTitle(String sessionId, String title) {
        ChatSession session = getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId));
        if (session == null || title == null || title.isBlank()) {
            return;
        }
        session.setTitle(title.trim());
        updateById(session);  // 自动填充 updateTime/updateBy
        log.info("会话标题已更新: sessionId={}, title={}", sessionId, title);
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

    @Override
    public List<ChatSessionVO> listUserSessions(Long userId) {
        // 查用户所有未删除会话，按更新时间倒序
        List<ChatSession> sessions = list(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .orderByDesc(ChatSession::getUpdateTime));

        if (sessions.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查原始文件名
        Set<Long> fileIds = sessions.stream()
                .map(ChatSession::getOriginalFileId)
                .collect(Collectors.toSet());
        Map<Long, String> fileNameMap = new HashMap<>();
        if (!fileIds.isEmpty()) {
            List<FileRecord> files = fileRecordMapper.selectBatchIds(fileIds);
            for (FileRecord f : files) {
                fileNameMap.put(f.getId(), f.getFileName());
            }
        }

        // 取最后一条消息预览
        Set<String> sessionIds = sessions.stream()
                .map(ChatSession::getSessionId)
                .collect(Collectors.toSet());
        Map<String, String> lastMsgMap = new HashMap<>();
        for (String sid : sessionIds) {
            ChatMessage lastMsg = chatMessageMapper.selectOne(new LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getSessionId, sid)
                    .orderByDesc(ChatMessage::getCreateTime)
                    .last("LIMIT 1"));
            if (lastMsg != null && lastMsg.getContent() != null) {
                String preview = lastMsg.getContent();
                if (preview.length() > 50) {
                    preview = preview.substring(0, 50) + "...";
                }
                lastMsgMap.put(sid, preview);
            }
        }

        return sessions.stream().map(s -> ChatSessionVO.builder()
                .sessionId(s.getSessionId())
                .title(s.getTitle())
                .projectId(s.getProjectId())
                .originalFileId(s.getOriginalFileId())
                .currentFileId(s.getCurrentFileId())
                .originalFileName(fileNameMap.getOrDefault(s.getOriginalFileId(), ""))
                .docType(s.getDocType())
                .messageCount(s.getMessageCount())
                .lastMessagePreview(lastMsgMap.getOrDefault(s.getSessionId(), ""))
                .createTime(s.getCreateTime())
                .updateTime(s.getUpdateTime())
                .build()).collect(Collectors.toList());
    }

    @Override
    public ChatSessionDetailVO getSessionDetail(String sessionId) {
        ChatSession session = getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId));
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在: " + sessionId);
        }

        // 查原始文件名
        String originalFileName = "";
        FileRecord originalFile = fileRecordMapper.selectById(session.getOriginalFileId());
        if (originalFile != null) {
            originalFileName = originalFile.getFileName();
        }

        // 查所有消息，按时间正序
        List<ChatMessage> messages = getMessages(sessionId);
        List<Map<String, Object>> msgList = new ArrayList<>();
        for (ChatMessage msg : messages) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", msg.getId());
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            m.put("fileId", msg.getFileId());
            m.put("createTime", msg.getCreateTime() != null ? msg.getCreateTime().toString() : null);
            msgList.add(m);
        }

        return ChatSessionDetailVO.builder()
                .sessionId(session.getSessionId())
                .title(session.getTitle())
                .projectId(session.getProjectId())
                .originalFileId(session.getOriginalFileId())
                .currentFileId(session.getCurrentFileId())
                .originalFileName(originalFileName)
                .docType(session.getDocType())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .messages(msgList)
                .build();
    }
}
