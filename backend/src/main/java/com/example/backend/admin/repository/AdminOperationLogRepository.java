package com.example.backend.admin.repository;

import com.example.backend.admin.entity.AdminOperationLog;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Collections;
import java.util.stream.Collectors;

@Repository
public class AdminOperationLogRepository {

    private final ConcurrentHashMap<Long, AdminOperationLog> storage = new ConcurrentHashMap<Long, AdminOperationLog>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized AdminOperationLog save(AdminOperationLog log) {
        if (log.getId() == null) {
            log.setId(idGenerator.getAndIncrement());
        }
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(LocalDateTime.now());
        }
        storage.put(log.getId(), log);
        return log;
    }

    public List<AdminOperationLog> findAll() {
        return storage.values().stream()
                .sorted((a, b) -> {
                    int cmp = b.getCreatedAt().compareTo(a.getCreatedAt());
                    return cmp != 0 ? cmp : Long.compare(b.getId(), a.getId());
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(ArrayList::new),
                        Collections::unmodifiableList));
    }
}
