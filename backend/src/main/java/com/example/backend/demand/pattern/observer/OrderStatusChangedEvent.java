package com.example.backend.demand.pattern.observer;

import com.example.backend.common.enums.OrderStatus;

public class OrderStatusChangedEvent {
    private final Long orderId;
    private final Long demandId;
    private final Long publisherId;
    private final Long acceptorId;
    private final OrderStatus fromStatus;
    private final OrderStatus toStatus;
    private final Long operatorId;
    private final String remark;

    public OrderStatusChangedEvent(
            Long orderId,
            Long demandId,
            Long publisherId,
            Long acceptorId,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            Long operatorId,
            String remark) {
        this.orderId = orderId;
        this.demandId = demandId;
        this.publisherId = publisherId;
        this.acceptorId = acceptorId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.operatorId = operatorId;
        this.remark = remark;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getDemandId() {
        return demandId;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public Long getAcceptorId() {
        return acceptorId;
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

    public String getRemark() {
        return remark;
    }
}
