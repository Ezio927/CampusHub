/**
 * 认证接口 — 对应后端 AuthController (/api/auth)
 *
 * 对外接口：
 *   register({ username, password, studentId, realName, phone?, email? })
 *     → POST /api/auth/register → { id, username, role, status, creditScore }
 *   login({ username, password })
 *     → POST /api/auth/login → { token, expiresAt, user }
 *   logout()
 *     → POST /api/auth/logout
 *
 * 待完成：
 *   - 注册成功后不自动登录，引导用户去登录页
 *   - 登录成功后将 token 写入 auth store + localStorage
 *   - 处理 40904（用户名已存在）、40905（学号已注册）
 */
