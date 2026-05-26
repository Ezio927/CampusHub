/**
 * 评价服务接口
 *
 * 对外接口：
 *   ReviewDto submitReview(Long userId, ReviewSubmitDto req)
 *                        — 提交评价（≤3 星自动触发仲裁工单创建）
 *   Page<ReviewDto> getUserReviews(Long userId, Pageable pageable)
 *                        — 某用户收到的评价
 *   List<ReviewDto> getOrderReviews(Long orderId)
 *                        — 某订单的双向评价
 *
 * 约束：每个订单每人只能评价一次（orderId + reviewerId 唯一）
 */
