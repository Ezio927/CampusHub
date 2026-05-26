/**
 * 订单数据访问层
 *
 * 自定义查询：
 *   findByDemandId(Long) → Optional<Order>
 *   findByPublisherIdAndStatus(Long, OrderStatus, Pageable) — 我发布的订单
 *   findByAcceptorIdAndStatus(Long, OrderStatus, Pageable)  — 我接单的订单
 *   findByStatusAndAutoConfirmDeadlineBefore(OrderStatus, LocalDateTime) — 超时自动确认用
 */
