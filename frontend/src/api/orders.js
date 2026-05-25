/**
 * 订单接口 — 对应后端 OrderController (/api/orders)
 *
 * 对外接口：
 *   accept(demandId)              → POST /api/orders → 接单
 *   getMyOrders({ role, status }) → GET  /api/orders?role=&status=&page=&size=
 *   getDetail(id)                 → GET  /api/orders/{id}
 *   markDelivered(id)             → PUT  /api/orders/{id}/deliver
 *   confirm(id)                   → PUT  /api/orders/{id}/confirm
 *   cancel(id, reason)            → PUT  /api/orders/{id}/cancel
 *
 * 待完成：
 *   - accept 需拦截 40301（信用分不足）、40901（已被他人接单）
 *   - cancel 需区分响应 message（无责 vs 扣分）
 *   - getMyOrders 用于 MyOrdersView（发布方/接单方两个 tab）
 */
