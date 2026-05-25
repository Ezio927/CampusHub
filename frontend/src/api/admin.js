/**
 * 管理后台接口 — 对应后端 AdminController (/api/admin)
 *
 * 对外接口：
 *   getDashboard()          → GET /api/admin/dashboard
 *   getUsers(params)        → GET /api/admin/users → 用户管理列表
 *   banUser(id, reason)     → PUT /api/admin/users/{id}/ban
 *   unbanUser(id)           → PUT /api/admin/users/{id}/unban
 *
 * 待完成：
 *   - 路由守卫保证仅 role=ADMIN 可调用
 *   - getDashboard 返回 totalUsers, activeDemands, completedOrders 等面板数据
 */
