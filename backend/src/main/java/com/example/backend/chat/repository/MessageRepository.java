package com.example.backend.chat.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.chat.entity.Message;
import com.example.backend.chat.mapper.MessageMapper;
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

    public List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByDesc(Message::getCreatedAt));
    }

    public long countByConversationIdAndSenderIdNotAndReadFalse(Long conversationId, Long senderId) {
        return mapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .ne(Message::getSenderId, senderId)
                        .eq(Message::getRead, false));
    }
}
