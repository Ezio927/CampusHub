/**
 * 评价接口 — 对应后端 ReviewController (/api/reviews)
 *
 * 对外接口：
 *   submit({ orderId, rating, content?, imageUrls? })
 *     → POST /api/reviews → ≤3 星需 content≥15字 + imageUrls≥1张
 *   getUserReviews(userId, page, size)
 *     → GET  /api/reviews/user/{userId}
 *   getOrderReviews(orderId)
 *     → GET  /api/reviews/order/{orderId}
 *
 * 待完成：
 *   - submit 前端先校验：≤3 星时 content.length>=15 && imageUrls.length>=1
 *   - 处理 40903（重复评价）
 */
