package com.example.backend.admin.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.admin.entity.AdminOperationLog;
import com.example.backend.admin.mapper.AdminOperationLogMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AdminOperationLogRepository {

    private final AdminOperationLogMapper mapper;

    public AdminOperationLogRepository(AdminOperationLogMapper mapper) {
        this.mapper = mapper;
    }

    public AdminOperationLog save(AdminOperationLog log) {
        if (log.getCreatedAt() == null) log.setCreatedAt(LocalDateTime.now());
        if (log.getId() == null) {
            mapper.insert(log);
        } else {
            mapper.updateById(log);
        }
        return log;
    }

    public List<AdminOperationLog> findAll() {
        return mapper.selectList(
                new LambdaQueryWrapper<AdminOperationLog>()
                        .orderByDesc(AdminOperationLog::getCreatedAt)
                        .orderByDesc(AdminOperationLog::getId));
    }
}
