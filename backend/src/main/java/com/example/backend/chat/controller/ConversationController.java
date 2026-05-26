/**
 * 聊天接口控制器 — /api/conversations
 *
 * 对外接口：
 *   POST /              — 获取或创建会话（需认证）
 *   GET  /              — 当前用户会话列表
 *   POST /{id}/messages — 发送消息
 *   GET  /{id}/messages — 获取历史消息（分页，倒序）
 *   PUT  /{id}/read     — 标记会话已读
 *
 * 依赖：ConversationService
 */
