package com.example.backend.chat.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.chat.entity.Message;
import com.example.backend.chat.mapper.MessageMapper;
import com.example.backend.common.enums.MessageStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepository {

    private final MessageMapper mapper;

    public MessageRepository(MessageMapper mapper) {
        this.mapper = mapper;
    }

    public Message save(Message m) {
        if (m.getCreatedAt() == null) m.setCreatedAt(LocalDateTime.now());
        if (m.getId() == null) {
            mapper.insert(m);
        } else {
            mapper.updateById(m);
        }
        return m;
    }

    public Optional<Message> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public Optional<Message> findBySenderIdAndClientMessageId(Long senderId, String clientMessageId) {
        if (clientMessageId == null || clientMessageId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.selectOne(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getSenderId, senderId)
                        .eq(Message::getClientMessageId, clientMessageId)
                        .last("LIMIT 1")));
    }

    public List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByDesc(Message::getCreatedAt));
    }

    public List<Message> findByConversationIdOrderByIdAsc(Long conversationId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByAsc(Message::getId));
    }

    public List<Message> findByConversationIdAndIdGreaterThanOrderByIdAsc(Long conversationId, Long afterId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .gt(Message::getId, afterId == null ? 0L : afterId)
                        .orderByAsc(Message::getId));
    }

    public List<Message> findByConversationIdOrderByIdDesc(Long conversationId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByDesc(Message::getId));
    }

    public List<Message> searchByConversationIdAndKeyword(Long conversationId, String keyword) {
        return mapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .like(Message::getContent, keyword)
                        .orderByAsc(Message::getId));
    }

    public Optional<Message> findLastByConversationId(Long conversationId) {
        return Optional.ofNullable(mapper.selectOne(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByDesc(Message::getId)
                        .last("LIMIT 1")));
    }

    public long countByConversationIdAndSenderIdNotAndReadFalse(Long conversationId, Long senderId) {
        return mapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .ne(Message::getSenderId, senderId)
                        .eq(Message::getRead, false));
    }

    public long countUnreadAfterLastRead(Long conversationId, Long userId, Long lastReadMessageId) {
        return mapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .ne(Message::getSenderId, userId)
                        .gt(Message::getId, lastReadMessageId == null ? 0L : lastReadMessageId)
                        .ne(Message::getStatus, MessageStatus.RECALLED));
    }
}
