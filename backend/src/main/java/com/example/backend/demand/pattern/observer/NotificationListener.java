/**
 * 通知监听器（观察者模式）
 *
 * 职责：监听 OrderStatusChangedEvent，根据事件类型自动创建对应的站内通知
 *
 * 映射：
 *   → ACCEPTED      → "您的需求已被接单" 发给发布者
 *   → PENDING_CONFIRM → "服务方已标记完成" 发给发布者
 *   → COMPLETED     → "订单已完成" 发给双方
 *   → CANCELLED     → "订单已取消" 发给另一方
 *
 * 调用 NotificationService 完成通知创建
 */
