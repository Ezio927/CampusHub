package com.example.backend.chat.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.chat.entity.MessageReport;
import com.example.backend.chat.mapper.MessageReportMapper;
import com.example.backend.common.enums.ReportStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MessageReportRepository {

    private final MessageReportMapper mapper;

    public MessageReportRepository(MessageReportMapper mapper) {
        this.mapper = mapper;
    }

    public MessageReport save(MessageReport report) {
        if (report.getCreatedAt() == null) {
            report.setCreatedAt(LocalDateTime.now());
        }
        if (report.getId() == null) {
            mapper.insert(report);
        } else {
            mapper.updateById(report);
        }
        return report;
    }

    public Optional<MessageReport> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public List<MessageReport> findByStatusOrderByCreatedAtAsc(ReportStatus status) {
        return mapper.selectList(new LambdaQueryWrapper<MessageReport>()
                .eq(MessageReport::getStatus, status)
                .orderByAsc(MessageReport::getCreatedAt));
    }

    public List<MessageReport> findAllOrderByCreatedAtDesc() {
        return mapper.selectList(new LambdaQueryWrapper<MessageReport>()
                .orderByDesc(MessageReport::getCreatedAt));
    }
}
