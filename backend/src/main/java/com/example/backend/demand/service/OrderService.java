package com.example.backend.demand.service;

import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.dto.AcceptOrderDto;
import com.example.backend.demand.dto.OrderCancelDto;
import com.example.backend.demand.dto.OrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto acceptDemand(Long userId, AcceptOrderDto request);

    Page<OrderDto> listMyOrders(Long userId, String role, OrderStatus status, Pageable pageable);

    OrderDto getDetail(Long orderId, Long userId);

    OrderDto markDelivered(Long orderId, Long userId);

    OrderDto confirmCompleted(Long orderId, Long userId);

    OrderDto cancelOrder(Long orderId, Long userId, OrderCancelDto request);

    void autoConfirmExpired();
}
