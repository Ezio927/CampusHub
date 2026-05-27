package com.example.backend.demand.repository;

import com.example.backend.demand.entity.OrderStatusLog;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class OrderStatusLogRepository {

    private final ConcurrentHashMap<Long, OrderStatusLog> storage = new ConcurrentHashMap<Long, OrderStatusLog>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized OrderStatusLog save(OrderStatusLog log) {
        if (log.getId() == null) {
            log.setId(idGenerator.getAndIncrement());
        }
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(LocalDateTime.now());
        }
        storage.put(log.getId(), log);
        return log;
    }

    public List<OrderStatusLog> findByOrderIdOrderByCreatedAtDesc(Long orderId) {
        return storage.values().stream()
                .filter(log -> log.getOrderId().equals(orderId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
