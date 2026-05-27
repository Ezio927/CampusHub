package com.example.backend.demand.pattern.observer;

import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.entity.Order;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public OrderEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publish(Order order, OrderStatus fromStatus, Long operatorId, String remark) {
        if (order == null) {
            return;
        }
        eventPublisher.publishEvent(new OrderStatusChangedEvent(
                order.getId(),
                order.getDemandId(),
                order.getPublisherId(),
                order.getAcceptorId(),
                fromStatus,
                order.getStatus(),
                operatorId,
                remark));
    }
}
