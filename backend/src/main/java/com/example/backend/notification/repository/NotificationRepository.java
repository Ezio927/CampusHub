package com.example.backend.notification.repository;

import com.example.backend.notification.entity.Notification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class NotificationRepository {

    private final ConcurrentHashMap<Long, Notification> storage = new ConcurrentHashMap<Long, Notification>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized Notification save(Notification notification) {
        if (notification.getId() == null) {
            notification.setId(idGenerator.getAndIncrement());
        }
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        storage.put(notification.getId(), notification);
        return notification;
    }

    public Optional<Notification> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return storage.values().stream()
                .filter(notification -> notification.getUserId().equals(userId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public long countByUserIdAndReadFalse(Long userId) {
        return storage.values().stream()
                .filter(notification -> notification.getUserId().equals(userId))
                .filter(notification -> !Boolean.TRUE.equals(notification.getRead()))
                .count();
    }
}
