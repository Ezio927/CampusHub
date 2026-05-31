package com.example.backend.chat.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.chat.entity.Conversation;
import com.example.backend.chat.mapper.ConversationMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ConversationRepository {

    private final ConversationMapper mapper;

    public ConversationRepository(ConversationMapper mapper) {
        this.mapper = mapper;
    }

    public Conversation save(Conversation c) {
        if (c.getCreatedAt() == null) c.setCreatedAt(LocalDateTime.now());
        if (c.getConversationKey() == null && c.getUser1Id() != null && c.getUser2Id() != null) {
            c.setConversationKey(buildKey(c.getUser1Id(), c.getUser2Id()));
        }
        if (c.getId() == null) {
            mapper.insert(c);
        } else {
            mapper.updateById(c);
        }
        return c;
    }

    public Optional<Conversation> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public Optional<Conversation> findByUser1IdAndUser2IdAndRelatedDemandId(Long user1Id, Long user2Id, Long relatedDemandId) {
        LambdaQueryWrapper<Conversation> query = new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUser1Id, user1Id)
                .eq(Conversation::getUser2Id, user2Id);
        if (relatedDemandId == null) {
            query.isNull(Conversation::getRelatedDemandId);
        } else {
            query.eq(Conversation::getRelatedDemandId, relatedDemandId);
        }
        List<Conversation> conversations = mapper.selectList(query.orderByAsc(Conversation::getId));
        if (conversations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(conversations.get(0));
    }

    public Optional<Conversation> findByUser1IdAndUser2Id(Long user1Id, Long user2Id) {
        List<Conversation> conversations = mapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUser1Id, user1Id)
                        .eq(Conversation::getUser2Id, user2Id)
                        .orderByAsc(Conversation::getId));
        if (conversations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(conversations.get(0));
    }

    public Optional<Conversation> findByConversationKey(String conversationKey) {
        List<Conversation> conversations = mapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getConversationKey, conversationKey)
                        .orderByAsc(Conversation::getId));
        if (conversations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(conversations.get(0));
    }

    public List<Conversation> findByUserIdOrderByLastMessageAtDesc(Long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .and(w -> w.eq(Conversation::getUser1Id, userId).or().eq(Conversation::getUser2Id, userId))
                        .orderByDesc(Conversation::getLastMessageAt)
                        .orderByDesc(Conversation::getCreatedAt));
    }

    public static String buildKey(Long user1Id, Long user2Id) {
        Long left = Math.min(user1Id, user2Id);
        Long right = Math.max(user1Id, user2Id);
        return left + ":" + right;
    }
}
