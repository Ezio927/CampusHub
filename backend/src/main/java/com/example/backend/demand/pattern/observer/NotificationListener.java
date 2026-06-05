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
                    "需求已被接单",
                    "你的需求已被接单，请及时关注订单进度。",
                    event.getOrderId());
        } else if (event.getToStatus() == OrderStatus.PENDING_CONFIRM) {
            notificationService.createNotification(
                    event.getPublisherId(),
                    NotificationType.ORDER_COMPLETED,
                    "订单待确认",
                    "订单已完成，正在等待你确认。",
                    event.getOrderId());
        } else if (event.getToStatus() == OrderStatus.COMPLETED) {
            notificationService.createNotification(
                    event.getAcceptorId(),
                    NotificationType.ORDER_COMPLETED,
                    "订单已完成",
                    "发布方已确认订单完成。",
                    event.getOrderId());
        } else if (event.getToStatus() == OrderStatus.CANCELLED) {
            Long receiverId = event.getOperatorId() != null && event.getOperatorId().equals(event.getPublisherId())
                    ? event.getAcceptorId()
                    : event.getPublisherId();
            notificationService.createNotification(
                    receiverId,
                    NotificationType.ORDER_CANCELLED,
                    "订单已取消",
                    event.getRemark() == null ? "订单已取消。" : event.getRemark(),
                    event.getOrderId());
        }
    }
}
