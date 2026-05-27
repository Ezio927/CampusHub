package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.entity.Order;

import java.util.Collections;
import java.util.List;

public class CancelledStrategy implements IOrderStateStrategy {

    @Override
    public boolean supports(OrderStatus status) {
        return status == OrderStatus.CANCELLED;
    }

    @Override
    public List<OrderStatus> nextStatuses() {
        return Collections.emptyList();
    }

    @Override
    public void transition(Order order, OrderStatus nextStatus) {
        order.setStatus(nextStatus);
    }
}
