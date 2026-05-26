/**
 * 信用分服务接口
 *
 * 对外接口：
 *   boolean checkCredit(Long userId)                       — 判断信用分 ≥ 60，返回可否接单
 *   void addCredit(Long userId, int amount, String reason, Long orderId)   — 加分
 *   void deductCredit(Long userId, int amount, String reason, Long orderId) — 扣分
 *   Page<CreditRecordDto> listRecords(Long userId, Pageable pageable) — 信用变动分页
 *
 * 约束：仅通过本 Service 操作信用分，其他模块禁止直接改 User.creditScore
 */
