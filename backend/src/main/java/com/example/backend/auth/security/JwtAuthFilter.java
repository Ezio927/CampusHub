/**
 * JWT 认证过滤器
 *
 * 职责：拦截所有请求，从 Authorization Header 提取 JWT，校验有效性，
 *       将用户信息写入 SecurityContext，后续 Controller 通过 @AuthenticationPrincipal 获取
 *
 * 流程：
 *   1. 提取 Header: Authorization: Bearer <token>
 *   2. 调 JwtTokenProvider.validate()
 *   3. 解析用户信息，构建 Authentication 对象
 *   4. 写入 SecurityContextHolder
 */
