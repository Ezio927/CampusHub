package com.example.backend.user.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.enums.QuotaRecordType;
import com.example.backend.user.entity.QuotaRecord;
import com.example.backend.user.mapper.QuotaRecordMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class QuotaRecordRepository {

    private final QuotaRecordMapper mapper;

    public QuotaRecordRepository(QuotaRecordMapper mapper) {
        this.mapper = mapper;
    }

    public QuotaRecord save(QuotaRecord record) {
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
        if (record.getId() == null) {
            mapper.insert(record);
        } else {
            mapper.updateById(record);
        }
        return record;
    }

    public List<QuotaRecord> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<QuotaRecord>()
                        .eq(QuotaRecord::getUserId, userId)
                        .orderByDesc(QuotaRecord::getCreatedAt)
                        .orderByDesc(QuotaRecord::getId));
    }

    public boolean existsByUserIdAndType(Long userId, QuotaRecordType type) {
        Long count = mapper.selectCount(new LambdaQueryWrapper<QuotaRecord>()
                .eq(QuotaRecord::getUserId, userId)
                .eq(QuotaRecord::getType, type));
        return count != null && count > 0;
    }

    public boolean existsByUserIdAndTypeAndCreatedAtBetween(Long userId, QuotaRecordType type,
                                                            LocalDateTime startInclusive,
                                                            LocalDateTime endExclusive) {
        Long count = mapper.selectCount(new LambdaQueryWrapper<QuotaRecord>()
                .eq(QuotaRecord::getUserId, userId)
                .eq(QuotaRecord::getType, type)
                .ge(QuotaRecord::getCreatedAt, startInclusive)
                .lt(QuotaRecord::getCreatedAt, endExclusive));
        return count != null && count > 0;
    }

    public boolean existsByUserIdAndRelatedDemandIdAndType(Long userId, Long demandId, QuotaRecordType type) {
        Long count = mapper.selectCount(new LambdaQueryWrapper<QuotaRecord>()
                .eq(QuotaRecord::getUserId, userId)
                .eq(QuotaRecord::getRelatedDemandId, demandId)
                .eq(QuotaRecord::getType, type));
        return count != null && count > 0;
    }

    public boolean existsByUserIdAndRelatedOrderIdAndType(Long userId, Long orderId, QuotaRecordType type) {
        Long count = mapper.selectCount(new LambdaQueryWrapper<QuotaRecord>()
                .eq(QuotaRecord::getUserId, userId)
                .eq(QuotaRecord::getRelatedOrderId, orderId)
                .eq(QuotaRecord::getType, type));
        return count != null && count > 0;
    }
}
