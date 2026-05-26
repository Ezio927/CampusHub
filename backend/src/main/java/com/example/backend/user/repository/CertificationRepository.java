/**
 * 认证数据访问层
 *
 * 自定义查询：
 *   findByUserId(Long) → Optional<Certification>
 *   findByStatus(CertStatus, Pageable) → Page<Certification>（管理员审核列表）
 */
