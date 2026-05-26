/**
 * 实名认证服务接口
 *
 * 对外接口：
 *   CertificationDto submit(Long userId, CertificationSubmitDto req) — 提交认证
 *   CertificationDto getMyCertification(Long userId)                 — 查询认证状态
 *   void review(Long id, boolean approve, String reason, Long reviewerId) — 管理员审核
 */
