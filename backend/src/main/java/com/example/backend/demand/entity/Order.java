/**
 * 订单实体 — orders 表
 *
 * 核心字段：id, demandId(FK unique), publisherId(FK), acceptorId(FK),
 *           status, acceptedAt, completedAt, cancelledAt, cancelReason,
 *           autoConfirmDeadline(24h), createdAt, updatedAt
 *
 * 实体方法：
 *   canAccept()               — 当前是否允许接单
 *   canCancel()               — 当前是否允许取消
 *   isAutoConfirmExpired()    — 是否触发 24h 自动确认
 */
