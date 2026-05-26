/**
 * 评价服务实现
 *
 * 待完成：
 *   - submitReview: 校验评分 1-5 → ≤3 星强制校验 content≥15 字 + imageUrls≥1 张
 *                   校验是否重复评价(orderId+reviewerId)
 *                   如果 ≤3 星 → 调用 ArbitrationService.createTicket 自动建工单
 */
