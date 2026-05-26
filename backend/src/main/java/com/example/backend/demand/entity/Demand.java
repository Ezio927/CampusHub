/**
 * 校园需求实体 — demands 表
 *
 * 核心字段：id, publisherId(FK), title, description, category, rewardAmount, rewardType,
 *           status, isUrgent, urgentDeadline, locationTag, createdAt, updatedAt
 *
 * 实体方法：
 *   isOpen()            — 判断需求是否可接单（status == OPEN）
 *   markAsAccepted()    — 标记已被接单（OPEN → ACCEPTED）
 *   markAsCompleted()   — 标记完成（→ COMPLETED）
 *   cancel()            — 取消需求（→ CANCELLED）
 *
 * 设计说明：不用 UrgentDemand 子类（违反 LSP 已修正），isUrgent + urgentDeadline 用组合方式嵌入
 */
