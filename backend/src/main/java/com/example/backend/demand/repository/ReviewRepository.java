package com.example.backend.demand.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.demand.entity.Review;
import com.example.backend.demand.mapper.ReviewMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepository {

    private final ReviewMapper mapper;

    public ReviewRepository(ReviewMapper mapper) {
        this.mapper = mapper;
    }

    public Review save(Review r) {
        if (r.getCreatedAt() == null) r.setCreatedAt(LocalDateTime.now());
        if (r.getId() == null) {
            mapper.insert(r);
        } else {
            mapper.updateById(r);
        }
        return r;
    }

    public Optional<Review> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public Optional<Review> findByOrderIdAndReviewerId(Long orderId, Long reviewerId) {
        return Optional.ofNullable(mapper.selectOne(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getOrderId, orderId)
                        .eq(Review::getReviewerId, reviewerId)));
    }

    public List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getRevieweeId, revieweeId)
                        .orderByDesc(Review::getCreatedAt));
    }

    public List<Review> findByOrderIdOrderByCreatedAtDesc(Long orderId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getOrderId, orderId)
                        .orderByDesc(Review::getCreatedAt));
    }

    public List<Review> findAll() {
        return mapper.selectList(null);
    }
}
