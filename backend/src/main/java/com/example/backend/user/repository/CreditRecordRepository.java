package com.example.backend.user.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.user.entity.CreditRecord;
import com.example.backend.user.mapper.CreditRecordMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CreditRecordRepository {

    private final CreditRecordMapper mapper;

    public CreditRecordRepository(CreditRecordMapper mapper) {
        this.mapper = mapper;
    }

    public CreditRecord save(CreditRecord r) {
        if (r.getCreatedAt() == null) r.setCreatedAt(LocalDateTime.now());
        if (r.getId() == null) {
            mapper.insert(r);
        } else {
            mapper.updateById(r);
        }
        return r;
    }

    public List<CreditRecord> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<CreditRecord>()
                        .eq(CreditRecord::getUserId, userId)
                        .orderByDesc(CreditRecord::getCreatedAt));
    }
}
