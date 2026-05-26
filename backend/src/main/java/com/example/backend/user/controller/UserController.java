/**
 * 用户接口控制器 — /api/users
 *
 * 对外接口：
 *   GET  /me               — 获取当前用户完整信息（需认证，phone/email 全量返回）
 *   PUT  /me               — 更新个人资料（需认证，仅更新传入字段）
 *   GET  /{id}             — 获取他人公开信息（phone/email 脱敏）
 *   GET  /me/credit-records — 当前用户信用分变动分页列表
 *
 * 依赖：UserService, CreditService
 */
