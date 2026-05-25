/**
 * 通知状态管理
 *
 * 职责：维护未读通知计数，供 NavBar 角标使用
 *
 * 对外接口：
 *   state:
 *     unreadCount   — 未读通知数量
 *     notifications — 通知分页列表
 *   actions:
 *     fetchUnreadCount()      — 请求 GET /api/notifications/unread-count
 *     fetchNotifications()    — 请求通知列表
 *     markRead(id)            — 单条已读
 *     markAllRead()           — 全部已读
 *
 * 待完成：
 *   - 订单状态变化后需重新拉取 unreadCount
 *   - 可选：轮询或 WebSocket 实时更新
 */
