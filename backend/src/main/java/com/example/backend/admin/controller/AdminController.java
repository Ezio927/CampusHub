/**
 * 管理后台控制器 — /api/admin
 *
 * 对外接口：
 *   GET  /dashboard      — 平台数据看板（totalUsers, activeDemands, completedOrders 等）
 *   GET  /users          — 用户管理列表（支持 keyword/status 筛选 + 分页）
 *   PUT  /users/{id}/ban — 封禁用户（需填原因）
 *   PUT  /users/{id}/unban — 解封用户
 *
 * 依赖：AdminService
 * 鉴权：所有接口需要 ADMIN 角色
 */
