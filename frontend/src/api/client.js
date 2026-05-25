/**
 * Axios 实例 + 拦截器
 *
 * 职责：创建 axios 实例，统一管理 baseURL、超时、JWT 注入、错误处理
 *
 * 对外接口（默认导出）：
 *   axios 实例，供所有 api/*.js 引用
 *
 * 待完成：
 *   - baseURL：开发 /api → Vite 代理到 localhost:8080
 *   - 请求拦截器：从 auth store 取 token → Authorization: Bearer <token>
 *   - 响应拦截器：code !== 20000 时，按错误码段处理
 *     · 40100~40199 → 清 token，跳 /login
 *     · 40300~40399 → Toast 提示权限不足
 *     · 50000~50099 → Toast "服务器异常,请稍后重试"
 */
