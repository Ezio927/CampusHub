/**
 * 订单服务实现
 *
 * 待完成：
 *   - acceptDemand: 校验信用分(CreditService) → 校验实名 → 乐观锁更新 Demand 状态 → 创建 Order
 *   - 状态流转通过 OrderStateContext 策略模式，不在 Service 内 if-else
 *   - 状态变更后通过 OrderEventPublisher 发布事件（通知/信用分/需求状态解耦处理）
 *   - cancelOrder: 判断是否 10 分钟内 → 无责 / 扣分(CreditService)
 *   - autoConfirmExpired: 定时任务扫 PENDING_CONFIRM 超时订单
 */
