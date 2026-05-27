package com.example.backend.demand.repository;

import com.example.backend.demand.entity.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Long> {
    List<OrderStatusLog> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    List<OrderStatusLog> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
