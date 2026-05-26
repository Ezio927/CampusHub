/**
 * 订单服务接口
 *
 * 对外接口：
 *   OrderDto acceptDemand(Long userId, Long demandId)
 *                        — 接单（校验信用分、实名、需求状态、并发冲突）
 *   Page<OrderDto> getMyOrders(Long userId, String role, String status, Pageable pageable)
 *                        — 我的订单（按发布方/接单方 + 状态筛选）
 *   OrderDto getDetail(Long orderId, Long userId)
 *                        — 订单详情（仅参与方可看）
 *   void markDelivered(Long orderId, Long userId)
 *                        — 服务方标记已送达
 *   void confirmCompleted(Long orderId, Long userId)
 *                        — 需求方确认验收
 *   void cancelOrder(Long orderId, Long userId, String reason)
 *                        — 取消（10 分钟无责 / 超时扣信用分）
 *   void autoConfirmExpired()
 *                        — 定时任务：24h 未确认自动完成
 *
 * 约束：跨模块调用必须走 Service 接口，禁止直接注入其他模块的 Repository
 */
