/**
 * 用户接口 — 对应后端 UserController (/api/users)
 *
 * 对外接口：
 *   getMyProfile()       → GET /api/users/me → 当前用户完整信息
 *   updateProfile(data)  → PUT /api/users/me → 更新后用户信息
 *   getUserPublic(id)    → GET /api/users/{id} → 他人公开信息（脱敏）
 *   getCreditRecords()   → GET /api/users/me/credit-records → 信用分变动分页
 *
 * 待完成：
 *   - getMyProfile 用于 ProfileView 初始化
 *   - getUserPublic 用于查看发布者/接单者信息
 */
