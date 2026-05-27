package com.example.backend.demand.repository;

import com.example.backend.demand.entity.DemandImage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class DemandImageRepository {

    private final ConcurrentHashMap<Long, DemandImage> storage = new ConcurrentHashMap<Long, DemandImage>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized DemandImage save(DemandImage image) {
        if (image.getId() == null) {
            image.setId(idGenerator.getAndIncrement());
        }
        storage.put(image.getId(), image);
        return image;
    }

    public Optional<DemandImage> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<DemandImage> findByDemandIdOrderBySortOrderAsc(Long demandId) {
        return storage.values().stream()
                .filter(image -> image.getDemandId().equals(demandId))
                .sorted(Comparator.comparing(DemandImage::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void deleteByDemandId(Long demandId) {
        storage.entrySet().removeIf(entry -> entry.getValue().getDemandId().equals(demandId));
    }
}
