/**
 * 信用分监听器（观察者模式）
 *
 * 职责：监听 OrderStatusChangedEvent，自动处理信用分变动
 *
 * 规则：
 *   → COMPLETED  → 接单方 +5 分
 *   → CANCELLED 且超时 → 取消方 -10 分
 *
 * 调用 CreditService 完成信用分操作
 */
