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
        return Optional.ofNullable(mapper.selectOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUser1Id, user1Id)
                        .eq(Conversation::getUser2Id, user2Id)
                        .eq(Conversation::getRelatedDemandId, relatedDemandId)));
    }

    public List<Conversation> findByUserIdOrderByLastMessageAtDesc(Long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .and(w -> w.eq(Conversation::getUser1Id, userId).or().eq(Conversation::getUser2Id, userId))
                        .orderByDesc(Conversation::getLastMessageAt));
    }
}
