/**
 * 聊天服务接口
 *
 * 对外接口：
 *   ConversationDto getOrCreate(Long userId, Long targetUserId, Long demandId)
 *                        — 获取已有会话或新建
 *   List<ConversationDto> listConversations(Long userId)
 *                        — 会话列表（含最后一条消息 + 未读数）
 *   MessageDto sendMessage(Long conversationId, Long senderId, String content, MessageType type)
 *                        — 发送消息
 *   Page<MessageDto> getMessages(Long conversationId, Pageable pageable)
 *                        — 历史消息（倒序分页）
 *   void markRead(Long conversationId, Long userId)
 *                        — 标记会话已读
 */
