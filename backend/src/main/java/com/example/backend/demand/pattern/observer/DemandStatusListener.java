/**
 * 需求状态同步监听器（观察者模式）
 *
 * 职责：监听 OrderStatusChangedEvent，同步更新关联 Demand 的状态
 *
 * 映射：
 *   订单状态变化 → 对应更新 Demand.status
 */
