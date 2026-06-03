package com.example.backend.user.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.enums.CertStatus;
import com.example.backend.user.entity.BanAppeal;
import com.example.backend.user.mapper.BanAppealMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class BanAppealRepository {

    private final BanAppealMapper mapper;

    public BanAppealRepository(BanAppealMapper mapper) {
        this.mapper = mapper;
    }

    public BanAppeal save(BanAppeal a) {
        if (a.getCreatedAt() == null) a.setCreatedAt(LocalDateTime.now());
        if (a.getId() == null) mapper.insert(a);
        else mapper.updateById(a);
        return a;
    }

    public Optional<BanAppeal> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public List<BanAppeal> findByStatusOrderByCreatedAtAsc(CertStatus status) {
        return mapper.selectList(new LambdaQueryWrapper<BanAppeal>()
                .eq(BanAppeal::getStatus, status)
                .orderByAsc(BanAppeal::getCreatedAt));
    }

    public Optional<BanAppeal> findPendingByUserId(Long userId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<BanAppeal>()
                .eq(BanAppeal::getUserId, userId)
                .eq(BanAppeal::getStatus, CertStatus.PENDING)
                .orderByDesc(BanAppeal::getCreatedAt)
                .last("LIMIT 1")));
    }

    public Optional<BanAppeal> findLatestByUserId(Long userId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<BanAppeal>()
                .eq(BanAppeal::getUserId, userId)
                .orderByDesc(BanAppeal::getCreatedAt)
                .last("LIMIT 1")));
    }
}
