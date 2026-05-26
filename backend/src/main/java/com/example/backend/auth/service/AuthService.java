/**
 * 认证服务接口
 *
 * 对外接口：
 *   UserDto register(RegisterRequest req)       — 注册新用户
 *   TokenResponse login(LoginRequest req)        — 验证用户 + 签发票据
 *   void logout(String token)                    — 登出（可选黑名单）
 *   boolean validateToken(String token)          — 校验 Token 有效性
 */
