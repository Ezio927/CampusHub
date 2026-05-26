/**
 * 智能匹配器接口（组合策略模式）
 *
 * 为需求推荐合适的接单用户，支持多维度打分
 *
 * 对外接口：
 *   List<User> match(Demand demand)    — 返回候选用户列表
 *   double score(User user, Demand demand) — 单维度评分
 */
