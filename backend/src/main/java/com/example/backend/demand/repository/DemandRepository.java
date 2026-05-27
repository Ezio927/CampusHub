package com.example.backend.demand.repository;

import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.demand.entity.Demand;
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
public class DemandRepository {

    private final ConcurrentHashMap<Long, Demand> storage = new ConcurrentHashMap<Long, Demand>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized Demand save(Demand demand) {
        if (demand.getId() == null) {
            demand.setId(idGenerator.getAndIncrement());
        }
        if (demand.getCreatedAt() == null) {
            demand.setCreatedAt(LocalDateTime.now());
        }
        demand.setUpdatedAt(LocalDateTime.now());
        storage.put(demand.getId(), demand);
        return demand;
    }

    public Optional<Demand> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Demand> findAll() {
        return Collections.unmodifiableList(new ArrayList<Demand>(storage.values()));
    }

    public List<Demand> findByPublisherIdOrderByCreatedAtDesc(Long publisherId) {
        return storage.values().stream()
                .filter(demand -> demand.getPublisherId().equals(publisherId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<Demand> findByStatusAndCategory(DemandStatus status, DemandCategory category) {
        return storage.values().stream()
                .filter(demand -> status == null || demand.getStatus() == status)
                .filter(demand -> category == null || demand.getCategory() == category)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }
}
