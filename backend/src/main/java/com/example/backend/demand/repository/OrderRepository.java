package com.example.backend.demand.repository;

import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.entity.Order;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class OrderRepository {

    private final ConcurrentHashMap<Long, Order> storage = new ConcurrentHashMap<Long, Order>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public synchronized Order save(Order order) {
        if (order.getId() == null) {
            order.setId(idGenerator.getAndIncrement());
        }
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        order.setUpdatedAt(LocalDateTime.now());
        storage.put(order.getId(), order);
        return order;
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Order> findByDemandId(Long demandId) {
        return storage.values().stream().filter(order -> order.getDemandId().equals(demandId)).findFirst();
    }

    public List<Order> findByPublisherIdAndStatus(Long publisherId, OrderStatus status) {
        return filterByUserAndStatus(publisherId, status, true);
    }

    public List<Order> findByAcceptorIdAndStatus(Long acceptorId, OrderStatus status) {
        return filterByUserAndStatus(acceptorId, status, false);
    }

    public List<Order> findByStatusAndAutoConfirmDeadlineBefore(OrderStatus status, LocalDateTime deadline) {
        return storage.values().stream()
                .filter(order -> order.getStatus() == status)
                .filter(order -> order.getAutoConfirmDeadline() != null && order.getAutoConfirmDeadline().isBefore(deadline))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Order> findAll() {
        return Collections.unmodifiableList(new ArrayList<Order>(storage.values()));
    }

    private List<Order> filterByUserAndStatus(Long userId, OrderStatus status, boolean publisher) {
        return storage.values().stream()
                .filter(order -> publisher ? order.getPublisherId().equals(userId) : order.getAcceptorId().equals(userId))
                .filter(order -> status == null || order.getStatus() == status)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
