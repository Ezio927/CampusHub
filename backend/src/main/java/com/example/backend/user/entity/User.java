/**
 * 用户实体 — users 表
 *
 * 核心字段：id, username, passwordHash, phone(加密), studentId, realName,
 *           creditScore(初始100), role, status, createdAt
 *
 * 实体方法（业务行为在实体内部封装）：
 *   isCreditSufficient()    — 信用分是否 ≥ 60
 *   addCredit(int amount)   — 增加信用分
 *   deductCredit(int amount) — 扣除信用分
 */
