/**
 * 评价接口控制器 — /api/reviews
 *
 * 对外接口：
 *   POST /               — 提交评价（≤3 星需 content≥15 字 + imageUrls≥1 张）
 *   GET  /user/{userId}  — 查看某用户收到的评价列表
 *   GET  /order/{orderId} — 查看某订单的评价（仅订单参与方可看）
 *
 * 依赖：ReviewService
 */
