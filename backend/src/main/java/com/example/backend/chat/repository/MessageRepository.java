package com.example.backend.chat.repository;

import com.example.backend.chat.entity.Message;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class MessageRepository {

    private final ConcurrentHashMap<Long, Message> storage = new ConcurrentHashMap<Long, Message>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized Message save(Message message) {
        if (message.getId() == null) {
            message.setId(idGenerator.getAndIncrement());
        }
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }
        storage.put(message.getId(), message);
        return message;
    }

    public Optional<Message> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId) {
        return storage.values().stream()
                .filter(message -> message.getConversationId().equals(conversationId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public long countByConversationIdAndSenderIdNotAndReadFalse(Long conversationId, Long senderId) {
        return storage.values().stream()
                .filter(message -> message.getConversationId().equals(conversationId))
                .filter(message -> !message.getSenderId().equals(senderId))
                .filter(message -> !Boolean.TRUE.equals(message.getRead()))
                .count();
    }
}
