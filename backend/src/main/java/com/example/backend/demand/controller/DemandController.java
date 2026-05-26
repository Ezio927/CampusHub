/**
 * 需求接口控制器 — /api/demands
 *
 * 对外接口：
 *   POST /       — 发布需求（需实名认证）
 *   GET  /       — 浏览需求列表（支持 category/status/keyword/locationTag/isUrgent/sortBy 筛选 + 分页）
 *   GET  /{id}   — 获取需求详情
 *   PUT  /{id}   — 编辑需求（仅发布者 + OPEN 状态）
 *   PUT  /{id}/cancel — 取消需求（仅发布者 + OPEN 状态，视为删除）
 *
 * 依赖：DemandService
 */
