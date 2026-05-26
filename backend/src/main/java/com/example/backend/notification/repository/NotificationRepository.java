/**
 * 通知数据访问层
 *
 * 自定义查询：
 *   findByUserIdOrderByCreatedAtDesc(Long userId, Pageable) → Page<Notification>
 *   countByUserIdAndIsReadFalse(Long userId) → long
 */
