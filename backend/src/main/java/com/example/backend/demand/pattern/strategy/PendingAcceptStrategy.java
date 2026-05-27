package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.entity.Order;

import java.util.Arrays;
import java.util.List;

public class PendingAcceptStrategy implements IOrderStateStrategy {

    @Override
    public boolean supports(OrderStatus status) {
        return status == OrderStatus.PENDING_ACCEPT;
    }

    @Override
    public List<OrderStatus> nextStatuses() {
        return Arrays.asList(OrderStatus.ACCEPTED, OrderStatus.CANCELLED);
    }

    @Override
    public void transition(Order order, OrderStatus nextStatus) {
        order.setStatus(nextStatus);
    }
}
