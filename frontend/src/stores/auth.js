/**
 * 认证状态管理
 *
 * 职责：管理用户登录态、JWT token、当前用户基本信息
 *
 * 对外接口：
 *   state:
 *     token          — JWT 字符串
 *     currentUser    — { id, username, role, status, creditScore }
 *     isLoggedIn     — computed: !!token
 *   actions:
 *     login(username, password)   — 调 api/auth.js.login，写入 token + user
 *     register(data)              — 调 api/auth.js.register
 *     logout()                    — 清 token，清 currentUser，跳 /login
 *     restoreSession()            — 从 localStorage 恢复 token（页面刷新用）
 *
 * 待完成：
 *   - token 需同步写入 localStorage，页面刷新后 restoreSession 恢复
 *   - logout 需同时调后端的 /api/auth/logout
 */
