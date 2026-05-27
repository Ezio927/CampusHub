package com.example.backend.demand.repository;

import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByDemandId(Long demandId);

    boolean existsByDemandId(Long demandId);

    Page<Order> findByPublisherId(Long publisherId, Pageable pageable);

    Page<Order> findByPublisherIdAndStatus(Long publisherId, OrderStatus status, Pageable pageable);

    Page<Order> findByAcceptorId(Long acceptorId, Pageable pageable);

    Page<Order> findByAcceptorIdAndStatus(Long acceptorId, OrderStatus status, Pageable pageable);

    @Query("""
            select o from DemandOrder o
            where (o.publisherId = :userId or o.acceptorId = :userId)
              and (:status is null or o.status = :status)
            """)
    Page<Order> findParticipatedOrders(@Param("userId") Long userId, @Param("status") OrderStatus status, Pageable pageable);

    List<Order> findByStatusAndAutoConfirmDeadlineBefore(OrderStatus status, LocalDateTime deadline);
}
