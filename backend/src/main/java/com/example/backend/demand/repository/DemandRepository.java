/**
 * 需求数据访问层
 *
 * 自定义查询：
 *   findByStatusAndCategory(DemandStatus, DemandCategory, Pageable) — 核心大厅查询
 *   findByPublisherIdOrderByCreatedAtDesc(Long, Pageable) — 我发布的需求
 *   支持 Specification 动态组合筛选条件
 */
