/**
 * 实名认证接口控制器 — /api/certifications
 *
 * 对外接口：
 *   POST /           — 提交实名认证申请（需认证）
 *   GET  /me         — 查询自己的认证状态（未提交时 data 为 null）
 *   PUT  /{id}/review — 管理员审核认证（需 ADMIN 角色）
 *
 * 依赖：CertificationService
 */
