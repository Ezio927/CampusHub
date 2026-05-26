/**
 * 订单事件发布器（观察者模式）
 *
 * 职责：在订单状态变更后，通过 Spring ApplicationEventPublisher 发布事件，
 *       各 Listener 独立处理后续衍生逻辑（通知、信用分、需求状态同步）
 *
 * 对外接口：
 *   void publish(OrderStatusChangedEvent event)
 */
