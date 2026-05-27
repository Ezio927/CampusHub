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
            notificationService.create(
                    event.getPublisherId(),
                    NotificationType.ORDER_ACCEPTED,
                    "您的需求已被接单",
                    "用户 " + event.getAcceptorId() + " 已接下您的需求，订单号：" + event.getOrderId(),
                    event.getOrderId());
        } else if (event.getToStatus() == OrderStatus.PENDING_CONFIRM) {
            notificationService.create(
                    event.getPublisherId(),
                    NotificationType.ORDER_COMPLETED,
                    "服务方已标记送达",
                    "订单 " + event.getOrderId() + " 已送达，请及时确认验收。",
                    event.getOrderId());
        } else if (event.getToStatus() == OrderStatus.COMPLETED) {
            notificationService.create(
                    event.getAcceptorId(),
                    NotificationType.ORDER_COMPLETED,
                    "订单已完成",
                    "订单 " + event.getOrderId() + " 已确认完成，请关注评价与信用变化。",
                    event.getOrderId());
        } else if (event.getToStatus() == OrderStatus.CANCELLED) {
            Long receiverId = event.getOperatorId() != null && event.getOperatorId().equals(event.getPublisherId())
                    ? event.getAcceptorId()
                    : event.getPublisherId();
            notificationService.create(
                    receiverId,
                    NotificationType.ORDER_CANCELLED,
                    "订单已取消",
                    "订单 " + event.getOrderId() + " 已取消，原因：" + event.getRemark(),
                    event.getOrderId());
        }
    }
}
