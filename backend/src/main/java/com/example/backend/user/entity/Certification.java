/**
 * 实名认证实体 — certifications 表
 *
 * 核心字段：id, userId(FK unique), certType, studentId, cardImageUrl,
 *           status, rejectReason, reviewedBy, reviewedAt, createdAt
 *
 * 实体方法：
 *   approve(Long reviewerId)         — 审核通过
 *   reject(Long reviewerId, String reason) — 审核驳回
 */
