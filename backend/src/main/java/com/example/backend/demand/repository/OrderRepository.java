package com.example.backend.demand.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.mapper.OrderMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private final OrderMapper mapper;

    public OrderRepository(OrderMapper mapper) {
        this.mapper = mapper;
    }

    public Order save(Order o) {
        normalizeActiveDemandId(o);
        if (o.getCreatedAt() == null) o.setCreatedAt(LocalDateTime.now());
        o.setUpdatedAt(LocalDateTime.now());
        if (o.getId() == null) {
            mapper.insert(o);
        } else {
            mapper.updateById(o);
        }
        return o;
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public Optional<Order> findByDemandId(Long demandId) {
        List<Order> orders = mapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDemandId, demandId)
                        .orderByDesc(Order::getCreatedAt)
                        .orderByDesc(Order::getId));
        if (orders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(orders.get(0));
    }

    public Optional<Order> findActiveByDemandId(Long demandId) {
        List<Order> orders = mapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDemandId, demandId)
                        .in(Order::getStatus,
                                OrderStatus.ACCEPTED,
                                OrderStatus.PENDING_CONFIRM)
                        .orderByDesc(Order::getCreatedAt)
                        .orderByDesc(Order::getId));
        if (orders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(orders.get(0));
    }

    public List<Order> findByPublisherIdAndStatus(Long publisherId, OrderStatus status) {
        LambdaQueryWrapper<Order> q = new LambdaQueryWrapper<Order>()
                .eq(Order::getPublisherId, publisherId);
        if (status != null) q.eq(Order::getStatus, status);
        q.orderByDesc(Order::getCreatedAt);
        return mapper.selectList(q);
    }

    public List<Order> findByAcceptorIdAndStatus(Long acceptorId, OrderStatus status) {
        LambdaQueryWrapper<Order> q = new LambdaQueryWrapper<Order>()
                .eq(Order::getAcceptorId, acceptorId);
        if (status != null) q.eq(Order::getStatus, status);
        q.orderByDesc(Order::getCreatedAt);
        return mapper.selectList(q);
    }

    public List<Order> findByStatus(OrderStatus status) {
        LambdaQueryWrapper<Order> q = new LambdaQueryWrapper<Order>();
        if (status != null) q.eq(Order::getStatus, status);
        q.orderByDesc(Order::getCreatedAt);
        return mapper.selectList(q);
    }

    public List<Order> findByStatusAndAutoConfirmDeadlineBefore(OrderStatus status, LocalDateTime deadline) {
        return mapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, status)
                        .isNotNull(Order::getAutoConfirmDeadline)
                        .lt(Order::getAutoConfirmDeadline, deadline));
    }

    public List<Order> findByAcceptorId(Long acceptorId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getAcceptorId, acceptorId)
                        .orderByDesc(Order::getCreatedAt));
    }

    public List<Order> findHistoryByUserId(Long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .and(q -> q.eq(Order::getPublisherId, userId)
                                .or()
                                .eq(Order::getAcceptorId, userId))
                        .in(Order::getStatus, OrderStatus.COMPLETED, OrderStatus.CANCELLED)
                        .orderByDesc(Order::getUpdatedAt)
                        .orderByDesc(Order::getCreatedAt));
    }

    public List<Order> findAll() {
        return mapper.selectList(null);
    }

    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    private void normalizeActiveDemandId(Order order) {
        if (order == null) {
            return;
        }
        if (order.getStatus() == OrderStatus.ACCEPTED || order.getStatus() == OrderStatus.PENDING_CONFIRM) {
            order.setActiveDemandId(order.getDemandId());
        } else {
            order.setActiveDemandId(null);
        }
    }
}
