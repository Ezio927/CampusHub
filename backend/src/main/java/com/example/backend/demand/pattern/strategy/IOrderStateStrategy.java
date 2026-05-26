/**
 * 订单状态策略接口（策略模式）
 *
 * 每个订单状态（6种）各自实现本接口，定义该状态下的合法操作
 *
 * 对外接口：
 *   boolean canAccept(Order order)    — 是否可以接单
 *   boolean canComplete(Order order)  — 是否可以标记完成
 *   boolean canCancel(Order order)    — 是否可以取消
 *   boolean canConfirm(Order order)   — 是否可以确认验收
 *   List<OrderStatus> nextStatuses()  — 允许流转到哪些状态
 */
