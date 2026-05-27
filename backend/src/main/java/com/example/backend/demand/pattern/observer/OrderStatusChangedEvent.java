package com.example.backend.demand.pattern.observer;

import com.example.backend.common.enums.OrderStatus;

public class OrderStatusChangedEvent {

    private final Long orderId;
    private final OrderStatus fromStatus;
    private final OrderStatus toStatus;
    private final Long operatorId;

    public OrderStatusChangedEvent(Long orderId, OrderStatus fromStatus, OrderStatus toStatus, Long operatorId) {
        this.orderId = orderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.operatorId = operatorId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public OrderStatus getFromStatus() {
        return fromStatus;
    }

    public OrderStatus getToStatus() {
        return toStatus;
    }

    public Long getOperatorId() {
        return operatorId;
    }
}
