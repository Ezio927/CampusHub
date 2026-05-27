package com.example.backend.chat.repository;

import com.example.backend.chat.entity.Conversation;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ConversationRepository {

    private final ConcurrentHashMap<Long, Conversation> storage = new ConcurrentHashMap<Long, Conversation>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized Conversation save(Conversation conversation) {
        if (conversation.getId() == null) {
            conversation.setId(idGenerator.getAndIncrement());
        }
        if (conversation.getCreatedAt() == null) {
            conversation.setCreatedAt(LocalDateTime.now());
        }
        storage.put(conversation.getId(), conversation);
        return conversation;
    }

    public Optional<Conversation> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Conversation> findByUser1IdAndUser2IdAndRelatedDemandId(Long user1Id, Long user2Id, Long relatedDemandId) {
        return storage.values().stream()
                .filter(conversation -> conversation.getUser1Id().equals(user1Id) && conversation.getUser2Id().equals(user2Id))
                .filter(conversation -> relatedDemandId == null || (conversation.getRelatedDemandId() != null && conversation.getRelatedDemandId().equals(relatedDemandId)))
                .findFirst();
    }

    public List<Conversation> findByUserIdOrderByLastMessageAtDesc(Long userId) {
        return storage.values().stream()
                .filter(conversation -> conversation.getUser1Id().equals(userId) || conversation.getUser2Id().equals(userId))
                .sorted((a, b) -> {
                    LocalDateTime left = a.getLastMessageAt();
                    LocalDateTime right = b.getLastMessageAt();
                    if (left == null && right == null) {
                        return 0;
                    }
                    if (left == null) {
                        return 1;
                    }
                    if (right == null) {
                        return -1;
                    }
                    return right.compareTo(left);
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
