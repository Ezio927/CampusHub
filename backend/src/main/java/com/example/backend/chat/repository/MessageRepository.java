/**
 * 消息数据访问层
 *
 * 自定义查询：
 *   findByConversationIdOrderByCreatedAtDesc(Long, Pageable) → Page<Message>
 *   countByConversationIdAndSenderIdNotAndIsReadFalse(Long, Long) → long（未读计数）
 */
