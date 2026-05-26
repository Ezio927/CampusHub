/**
 * 仲裁服务接口
 *
 * 对外接口：
 *   ArbitrationDto createTicket(Long reporterId, ArbitrationCreateDto req)
 *                        — 创建工单（自动触发 or 手动申诉）
 *   Page<ArbitrationDto> listPending(Pageable pageable)
 *                        — 管理员待处理工单列表
 *   void resolve(Long ticketId, Long adminId, ArbitrationResolveDto req)
 *                        — 处理工单（驳回 / 惩罚发布方 / 惩罚接单方）
 */
