package com.example.backend.demand.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.demand.dto.DemandSearchCriteria;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.mapper.DemandMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DemandRepository {

    private final DemandMapper mapper;

    public DemandRepository(DemandMapper mapper) {
        this.mapper = mapper;
    }

    public Demand save(Demand d) {
        if (d.getCreatedAt() == null) d.setCreatedAt(LocalDateTime.now());
        d.setUpdatedAt(LocalDateTime.now());
        if (d.getId() == null) {
            mapper.insert(d);
        } else {
            mapper.updateById(d);
        }
        return d;
    }

    public Optional<Demand> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public List<Demand> findAll() {
        return mapper.selectList(null);
    }

    public List<Demand> findByPublisherIdOrderByCreatedAtDesc(Long publisherId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Demand>()
                        .eq(Demand::getPublisherId, publisherId)
                        .orderByDesc(Demand::getCreatedAt));
    }

    public List<Demand> findByStatusAndCategory(DemandStatus status, DemandCategory category) {
        LambdaQueryWrapper<Demand> q = new LambdaQueryWrapper<>();
        if (status != null) q.eq(Demand::getStatus, status);
        if (category != null) q.eq(Demand::getCategory, category);
        q.orderByDesc(Demand::getCreatedAt);
        return mapper.selectList(q);
    }

    public List<Demand> findOpenUrgentWithDeadlineBefore(LocalDateTime deadline) {
        return mapper.selectList(
                new LambdaQueryWrapper<Demand>()
                        .eq(Demand::getStatus, DemandStatus.OPEN)
                        .eq(Demand::getUrgent, true)
                        .isNotNull(Demand::getUrgentDeadline)
                        .lt(Demand::getUrgentDeadline, deadline));
    }

    public Page<Demand> searchByCriteria(DemandSearchCriteria criteria, int page, int size) {
        LambdaQueryWrapper<Demand> q = new LambdaQueryWrapper<>();
        if (criteria != null) {
            if (criteria.getPublisherId() != null) {
                q.eq(Demand::getPublisherId, criteria.getPublisherId());
            }
            if (criteria.getCategory() != null) {
                q.eq(Demand::getCategory, criteria.getCategory());
            }
            if (criteria.getStatus() != null) {
                q.eq(Demand::getStatus, criteria.getStatus());
            }
            if (criteria.getUrgent() != null) {
                q.eq(Demand::getUrgent, criteria.getUrgent());
            }
            if (!isBlank(criteria.getLocationTag())) {
                q.like(Demand::getLocationTag, criteria.getLocationTag());
            }
            if (!isBlank(criteria.getKeyword())) {
                q.and(w -> w.like(Demand::getTitle, criteria.getKeyword())
                        .or().like(Demand::getDescription, criteria.getKeyword()));
            }
            applySort(q, criteria.getSortBy(), criteria.getSortDir());
        } else {
            q.orderByDesc(Demand::getCreatedAt);
        }
        return mapper.selectPage(new Page<>(page + 1, size), q);
    }

    public List<Demand> findAllOpen() {
        return mapper.selectList(
                new LambdaQueryWrapper<Demand>()
                        .eq(Demand::getStatus, DemandStatus.OPEN)
                        .orderByDesc(Demand::getCreatedAt));
    }

    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    private void applySort(LambdaQueryWrapper<Demand> q, String sortBy, String sortDir) {
        boolean asc = "asc".equalsIgnoreCase(sortDir);
        if ("rewardAmount".equalsIgnoreCase(sortBy)) {
            if (asc) {
                q.orderByAsc(Demand::getRewardAmount);
            } else {
                q.orderByDesc(Demand::getRewardAmount);
            }
            q.orderByDesc(Demand::getCreatedAt);
        } else if ("urgentDeadline".equalsIgnoreCase(sortBy)) {
            if (asc) {
                q.orderByAsc(Demand::getUrgentDeadline);
            } else {
                q.orderByDesc(Demand::getUrgentDeadline);
            }
            q.orderByDesc(Demand::getCreatedAt);
        } else {
            q.orderByDesc(Demand::getCreatedAt);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
