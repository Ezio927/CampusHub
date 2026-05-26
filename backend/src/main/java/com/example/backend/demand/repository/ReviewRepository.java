/**
 * 评价数据访问层
 *
 * 自定义查询：
 *   findByOrderIdAndReviewerId(Long, Long) → Optional<Review>（重复评价校验）
 *   findByRevieweeIdOrderByCreatedAtDesc(Long, Pageable) → Page<Review>
 *   findByOrderId(Long) → List<Review>（某订单的双向评价）
 */
