package com.example.backend.demand.pattern.observer;

import com.example.backend.common.enums.NotificationType;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.getToStatus() == OrderStatus.ACCEPTED) {
            notificationService.createNotification(
                    event.getPublisherId(),
                    NotificationType.ORDER_ACCEPTED,
                    "Demand accepted",
                    "Your demand has been accepted.",
                    event.getOrderId());
        } else if (event.getToStatus() == OrderStatus.PENDING_CONFIRM) {
            notificationService.createNotification(
                    event.getPublisherId(),
                    NotificationType.ORDER_COMPLETED,
                    "Order pending confirmation",
                    "The order is delivered and waiting for your confirmation.",
                    event.getOrderId());
        } else if (event.getToStatus() == OrderStatus.COMPLETED) {
            notificationService.createNotification(
                    event.getAcceptorId(),
                    NotificationType.ORDER_COMPLETED,
                    "Order completed",
                    "The order has been confirmed by the publisher.",
                    event.getOrderId());
        } else if (event.getToStatus() == OrderStatus.CANCELLED) {
            Long receiverId = event.getOperatorId() != null && event.getOperatorId().equals(event.getPublisherId())
                    ? event.getAcceptorId()
                    : event.getPublisherId();
            notificationService.createNotification(
                    receiverId,
                    NotificationType.ORDER_CANCELLED,
                    "Order cancelled",
                    event.getRemark() == null ? "The order has been cancelled." : event.getRemark(),
                    event.getOrderId());
        }
    }
}
