/**
 * 认证服务实现
 *
 * 待完成：
 *   - register：校验用户名/学号唯一性 → BCrypt 加密密码 → 调 UserService 保存
 *   - login：查用户 → 验密码 → JwtTokenProvider 生成 token → 返回
 *   - logout：可选 Redis 黑名单
 */
