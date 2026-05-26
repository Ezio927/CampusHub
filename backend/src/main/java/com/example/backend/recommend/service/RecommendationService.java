/**
 * 智能推荐服务接口
 *
 * 对外接口：
 *   List<DemandDto> recommendDemands(Long userId)
 *                        — 根据用户偏好推荐需求
 *   List<UserDto> recommendAcceptors(Long demandId)
 *                        — 为需求推荐合适的接单用户（组合策略加权排序）
 *
 * 依赖：CompositeDemandMatcher
 */
