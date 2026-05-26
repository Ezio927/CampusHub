/**
 * 双向评价实体 — reviews 表
 *
 * 核心字段：id, orderId(FK), reviewerId(FK), revieweeId(FK),
 *           reviewerRole, rating(1-5), content, imageUrls(JSONB), createdAt
 *
 * 约束：同一订单同一人只能评价一次（orderId + reviewerId 唯一）
 *       评分 ≤ 3 星时 content 必须 ≥ 15 字 + imageUrls 至少 1 张（CHECK 约束）
 */
