/**
 * 通知接口 — 对应后端 NotificationController (/api/notifications)
 *
 * 对外接口：
 *   getList(page, size)  → GET  /api/notifications
 *   getUnreadCount()     → GET  /api/notifications/unread-count
 *   markRead(id)         → PUT  /api/notifications/{id}/read
 *   markAllRead()        → PUT  /api/notifications/read-all
 *
 * 待完成：
 *   - getUnreadCount 用于 NavBar 红点角标
 */
