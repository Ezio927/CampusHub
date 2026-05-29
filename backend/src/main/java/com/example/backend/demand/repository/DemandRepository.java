package com.example.backend.demand.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
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

    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
