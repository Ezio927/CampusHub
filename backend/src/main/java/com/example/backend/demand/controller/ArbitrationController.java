/**
 * 仲裁接口控制器 — /api/arbitrations
 *
 * 对外接口：
 *   POST /              — 创建仲裁工单（低分评价自动触发 / 用户手动申诉）
 *   GET  /              — 待处理工单列表（需 ADMIN 角色）
 *   PUT  /{id}/resolve  — 处理工单（需 ADMIN 角色，DISMISS / PENALIZE_PUBLISHER / PENALIZE_ACCEPTOR）
 *
 * 依赖：ArbitrationService
 */
