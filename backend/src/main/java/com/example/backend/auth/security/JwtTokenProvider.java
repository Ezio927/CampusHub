/**
 * JWT 令牌工具类
 *
 * 职责：JWT 的生成、解析、校验
 *
 * 对外接口：
 *   String generateToken(Long userId, String username, UserRole role)
 *   Long parseUserId(String token)
 *   boolean validate(String token)
 *
 * 待完成：
 *   - 使用 HMAC-SHA256 签名
 *   - Payload 包含 userId、username、role
 *   - 过期时间从 JwtConfig 读取
 */
