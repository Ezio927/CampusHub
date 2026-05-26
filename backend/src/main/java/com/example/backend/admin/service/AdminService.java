/**
 * 后台管理服务接口
 *
 * 对外接口：
 *   DashboardDto getDashboard()                     — 数据看板
 *   Page<UserDto> listUsers(String keyword, String status, Pageable) — 用户列表
 *   void banUser(Long userId, String reason)        — 封禁
 *   void unbanUser(Long userId)                     — 解封
 */
