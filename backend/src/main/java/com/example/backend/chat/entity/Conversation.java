/**
 * 聊天会话实体 — conversations 表
 *
 * 核心字段：id, user1Id(FK), user2Id(FK), relatedDemandId(FK),
 *           lastMessageContent, lastMessageAt, createdAt
 *
 * 约束：同一对用户+同一需求只能有一个会话（LEAST/GREATEST + COALESCE 联合唯一索引）
 */
