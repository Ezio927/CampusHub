/**
 * Spring Security 安全配置
 *
 * 职责：配置 SecurityFilterChain，将 JwtAuthFilter 注册到过滤器链
 *
 * 待完成：
 *   - 放行 /api/auth/login、/api/auth/register（无需认证）
 *   - 其余 /api/** 全部需要认证
 *   - 注册 JwtAuthFilter 在 UsernamePasswordAuthenticationFilter 之前
 *   - 配置 CorsFilter
 */
