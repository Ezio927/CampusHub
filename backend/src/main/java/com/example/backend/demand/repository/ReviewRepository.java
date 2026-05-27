package com.example.backend.demand.repository;

import com.example.backend.demand.entity.Review;
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
public class ReviewRepository {

    private final ConcurrentHashMap<Long, Review> storage = new ConcurrentHashMap<Long, Review>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized Review save(Review review) {
        if (review.getId() == null) {
            review.setId(idGenerator.getAndIncrement());
        }
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }
        storage.put(review.getId(), review);
        return review;
    }

    public Optional<Review> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Review> findByOrderIdAndReviewerId(Long orderId, Long reviewerId) {
        return storage.values().stream()
                .filter(review -> review.getOrderId().equals(orderId) && review.getReviewerId().equals(reviewerId))
                .findFirst();
    }

    public List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId) {
        return storage.values().stream()
                .filter(review -> review.getRevieweeId().equals(revieweeId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Review> findByOrderIdOrderByCreatedAtDesc(Long orderId) {
        return storage.values().stream()
                .filter(review -> review.getOrderId().equals(orderId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Review> findAll() {
        return Collections.unmodifiableList(new ArrayList<Review>(storage.values()));
    }
}
