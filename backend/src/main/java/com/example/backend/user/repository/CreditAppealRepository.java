package com.example.backend.user.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.enums.CertStatus;
import com.example.backend.user.entity.CreditAppeal;
import com.example.backend.user.mapper.CreditAppealMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class CreditAppealRepository {

    private final CreditAppealMapper mapper;

    public CreditAppealRepository(CreditAppealMapper mapper) {
        this.mapper = mapper;
    }

    public CreditAppeal save(CreditAppeal a) {
        if (a.getCreatedAt() == null) a.setCreatedAt(LocalDateTime.now());
        if (a.getId() == null) {
            mapper.insert(a);
        } else {
            mapper.updateById(a);
        }
        return a;
    }

    public Optional<CreditAppeal> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public List<CreditAppeal> findByStatusOrderByCreatedAtAsc(CertStatus status) {
        return mapper.selectList(
                new LambdaQueryWrapper<CreditAppeal>()
                        .eq(CreditAppeal::getStatus, status)
                        .orderByAsc(CreditAppeal::getCreatedAt));
    }

    public Optional<CreditAppeal> findPendingByUserId(Long userId) {
        return Optional.ofNullable(
                mapper.selectOne(new LambdaQueryWrapper<CreditAppeal>()
                        .eq(CreditAppeal::getUserId, userId)
                        .eq(CreditAppeal::getStatus, CertStatus.PENDING)
                        .orderByDesc(CreditAppeal::getCreatedAt)
                        .last("LIMIT 1")));
    }

    public Optional<CreditAppeal> findLatestByUserId(Long userId) {
        return Optional.ofNullable(
                mapper.selectOne(new LambdaQueryWrapper<CreditAppeal>()
                        .eq(CreditAppeal::getUserId, userId)
                        .orderByDesc(CreditAppeal::getCreatedAt)
                        .last("LIMIT 1")));
    }
}
