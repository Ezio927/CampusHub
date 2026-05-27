package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.entity.Order;

import java.util.List;

public interface IOrderStateStrategy {

    boolean supports(OrderStatus status);

    List<OrderStatus> nextStatuses();

    void transition(Order order, OrderStatus nextStatus);
}
