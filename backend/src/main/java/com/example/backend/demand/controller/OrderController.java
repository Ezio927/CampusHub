/**
 * 订单接口控制器 — /api/orders
 *
 * 对外接口：
 *   POST /           — 接单（需实名 + 信用 ≥ 60）
 *   GET  /           — 我的订单列表（按 role=PUBLISHER/ACCEPTOR + status 筛选 + 分页）
 *   GET  /{id}       — 订单详情（仅订单参与方可查看，含对方脱敏手机号）
 *   PUT  /{id}/deliver — 服务方标记已送达（IN_PROGRESS → PENDING_CONFIRM）
 *   PUT  /{id}/confirm — 需求方确认验收（PENDING_CONFIRM → COMPLETED）
 *   PUT  /{id}/cancel  — 取消订单（10 分钟内无责 / 超时扣信用分）
 *
 * 依赖：OrderService
 */
