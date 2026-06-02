package com.example.backend.demand.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.demand.entity.OrderStatusLog;
import com.example.backend.demand.mapper.OrderStatusLogMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrderStatusLogRepository {

    private final OrderStatusLogMapper mapper;

    public OrderStatusLogRepository(OrderStatusLogMapper mapper) {
        this.mapper = mapper;
    }

    public OrderStatusLog save(OrderStatusLog log) {
        if (log.getCreatedAt() == null) log.setCreatedAt(LocalDateTime.now());
        if (log.getId() == null) {
            mapper.insert(log);
        } else {
            mapper.updateById(log);
        }
        return log;
    }

    public List<OrderStatusLog> findByOrderIdOrderByCreatedAtDesc(Long orderId) {
        return mapper.selectList(
                new LambdaQueryWrapper<OrderStatusLog>()
                        .eq(OrderStatusLog::getOrderId, orderId)
                        .orderByDesc(OrderStatusLog::getCreatedAt));
    }

    public void deleteByOrderId(Long orderId) {
        mapper.delete(new LambdaQueryWrapper<OrderStatusLog>()
                .eq(OrderStatusLog::getOrderId, orderId));
    }
}
