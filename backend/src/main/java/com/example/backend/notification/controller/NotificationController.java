/**
 * 通知接口控制器 — /api/notifications
 *
 * 对外接口：
 *   GET  /              — 通知分页列表
 *   GET  /unread-count  — 未读通知数量（用于 NavBar 角标）
 *   PUT  /{id}/read     — 单条标记已读
 *   PUT  /read-all      — 全部标记已读
 *
 * 依赖：NotificationService
 */
