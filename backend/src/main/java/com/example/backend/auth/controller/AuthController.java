/**
 * 认证接口控制器 — /api/auth
 *
 * 对外接口：
 *   POST /register — 用户注册，无需认证
 *   POST /login    — 用户登录，返回 JWT token
 *   POST /logout   — 用户登出（需认证，可选：token 加入 Redis 黑名单）
 *
 * 依赖：AuthService
 */
