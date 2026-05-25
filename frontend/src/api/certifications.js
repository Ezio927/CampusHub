/**
 * 实名认证接口 — 对应后端 CertificationController (/api/certifications)
 *
 * 对外接口：
 *   submit({ certType, studentId, cardImageUrl? })
 *     → POST /api/certifications → 提交认证申请
 *   getMyCertification()
 *     → GET  /api/certifications/me → 查询认证状态
 *   review(id, { approve, reason? })
 *     → PUT  /api/certifications/{id}/review → 管理员审核
 *
 * 待完成：
 *   - getMyCertification 返回 null 时表示未提交认证
 *   - review 仅管理员调用
 */
