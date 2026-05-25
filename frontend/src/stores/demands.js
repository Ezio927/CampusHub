/**
 * 需求列表缓存
 *
 * 职责：缓存需求大厅列表数据，避免切页面后重复请求
 *
 * 对外接口：
 *   state:
 *     demands       — 当前列表数据
 *     filters       — 当前筛选条件 { category, status, keyword, locationTag, isUrgent, sortBy, sortDir }
 *     pagination    — { page, size, totalElements, totalPages }
 *   actions:
 *     fetchDemands()         — 请求列表并更新 state
 *     setFilter(key, value)  — 修改筛选条件并重新请求
 *     resetFilters()         — 恢复默认筛选
 *
 * 待完成：
 *   - 用户进 HomeView 时调 fetchDemands
 *   - setFilter 后自动重置 page 到 0
 */
