package com.example.backend.notification.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.notification.entity.Notification;
import com.example.backend.notification.mapper.NotificationMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class NotificationRepository {

    private final NotificationMapper mapper;

    public NotificationRepository(NotificationMapper mapper) {
        this.mapper = mapper;
    }

    public Notification save(Notification n) {
        if (n.getCreatedAt() == null) n.setCreatedAt(LocalDateTime.now());
        if (n.getId() == null) {
            mapper.insert(n);
        } else {
            mapper.updateById(n);
        }
        return n;
    }

    public Optional<Notification> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreatedAt));
    }

    public long countByUserIdAndReadFalse(Long userId) {
        return mapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getRead, false));
    }
}
