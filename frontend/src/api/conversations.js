/**
 * 聊天接口 — 对应后端 ConversationController (/api/conversations)
 *
 * 对外接口：
 *   getOrCreate({ targetUserId, demandId? })
 *     → POST /api/conversations → 获取/创建会话
 *   getList()
 *     → GET  /api/conversations → 当前用户会话列表
 *   sendMessage(id, { content, messageType })
 *     → POST /api/conversations/{id}/messages
 *   getMessages(id, page, size)
 *     → GET  /api/conversations/{id}/messages
 *
 * 待完成：
 *   - getList 需返回每会话的 unreadCount
 */
