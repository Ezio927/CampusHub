/**
 * 需求服务实现
 *
 * 待完成：
 *   - createDemand: 调 DemandFactory.createDemand(param) 校验+创建 → save
 *   - search: 动态查询，支持 category/status/keyword/locationTag/isUrgent 组合筛选
 *             支持 sortBy(createdAt/rewardAmount/urgentDeadline) + sortDir
 *   - 关键词搜索使用 GIN 索引或 LIKE 模糊匹配
 */
