package com.example.backend.user.repository;

import com.example.backend.user.entity.CreditRecord;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CreditRecordRepository {

    private final ConcurrentHashMap<Long, CreditRecord> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized CreditRecord save(CreditRecord record) {
        if (record.getId() == null) {
            record.setId(idGenerator.getAndIncrement());
        }
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
        storage.put(record.getId(), record);
        return record;
    }

    public List<CreditRecord> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return storage.values().stream()
                .filter(record -> record.getUserId().equals(userId))
                .sorted(Comparator.comparing(CreditRecord::getCreatedAt).reversed())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
