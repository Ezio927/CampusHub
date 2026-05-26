/**
 * 管理员操作日志 — admin_operation_logs 表
 *
 * 核心字段：id, adminId(FK), operation, targetType, targetId, detail, createdAt
 *
 * 所有后台高风险操作（封禁、解封、审核认证、处理仲裁）都写入此表用于审计。
 */
