package com.example.backend.user.repository;

import com.example.backend.common.enums.CertStatus;
import com.example.backend.user.entity.Certification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class CertificationRepository {

    private final ConcurrentHashMap<Long, Certification> storage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> userIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized Certification save(Certification certification) {
        if (certification.getId() == null) {
            certification.setId(idGenerator.getAndIncrement());
        }
        if (certification.getCreatedAt() == null) {
            certification.setCreatedAt(LocalDateTime.now());
        }
        storage.put(certification.getId(), certification);
        userIndex.put(certification.getUserId(), certification.getId());
        return certification;
    }

    public Optional<Certification> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Certification> findByUserId(Long userId) {
        Long certId = userIndex.get(userId);
        return certId == null ? Optional.empty() : findById(certId);
    }

    public List<Certification> findByStatus(CertStatus status) {
        return storage.values()
                .stream()
                .filter(certification -> certification.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Certification> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }
}
