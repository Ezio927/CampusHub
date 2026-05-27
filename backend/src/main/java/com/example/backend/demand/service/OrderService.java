package com.example.backend.demand.service;

import com.example.backend.demand.dto.AcceptOrderDto;
import com.example.backend.demand.dto.OrderCancelDto;
import com.example.backend.demand.dto.OrderDto;

import java.util.List;

public interface OrderService {

    OrderDto acceptDemand(Long userId, AcceptOrderDto req);

    List<OrderDto> list(Long userId, String role, String status);

    OrderDto getDetail(Long orderId, Long userId);

    OrderDto deliver(Long orderId, Long userId);

    OrderDto confirm(Long orderId, Long userId);

    OrderDto cancel(Long orderId, Long userId, OrderCancelDto req);
}
