package com.example.backend.user.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.enums.CertStatus;
import com.example.backend.user.entity.Certification;
import com.example.backend.user.mapper.CertificationMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class CertificationRepository {

    private final CertificationMapper mapper;

    public CertificationRepository(CertificationMapper mapper) {
        this.mapper = mapper;
    }

    public Certification save(Certification c) {
        if (c.getCreatedAt() == null) c.setCreatedAt(LocalDateTime.now());
        if (c.getId() == null) {
            mapper.insert(c);
        } else {
            mapper.updateById(c);
        }
        return c;
    }

    public Optional<Certification> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public Optional<Certification> findByUserId(Long userId) {
        return Optional.ofNullable(mapper.selectOne(
                new LambdaQueryWrapper<Certification>().eq(Certification::getUserId, userId)));
    }

    public List<Certification> findByStatus(CertStatus status) {
        return mapper.selectList(
                new LambdaQueryWrapper<Certification>().eq(Certification::getStatus, status));
    }

    public List<Certification> findAll() {
        return mapper.selectList(null);
    }
}
