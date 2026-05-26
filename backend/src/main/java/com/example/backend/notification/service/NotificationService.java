/**
 * 通知服务接口
 *
 * 对外接口：
 *   void createNotification(Long userId, NotificationType type, String title, String content, Long relatedId)
 *                        — 创建通知（由 Listener / Service 调用）
 *   Page<NotificationDto> listNotifications(Long userId, Pageable pageable)
 *                        — 分页列表
 *   long countUnread(Long userId) — 未读计数
 *   void markRead(Long notificationId) — 单条已读
 *   void markAllRead(Long userId) — 全部已读
 */
