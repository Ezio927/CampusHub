/**
 * 订单状态上下文（策略模式）
 *
 * 职责：维护 OrderStatus → IOrderStateStrategy 的映射 Map，
 *       根据订单当前状态选择对应策略，对外统一提供状态权限判断
 *
 * 对外接口：
 *   IOrderStateStrategy getStrategy(OrderStatus status)
 *   boolean canOperate(Order order, String action)
 */
