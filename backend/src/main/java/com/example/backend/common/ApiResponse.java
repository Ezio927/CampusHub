/**
 * 统一响应包装类
 *
 * 职责：所有 Controller 返回值都通过此类包装，保证前端收到的 JSON 格式统一
 *
 * 结构：
 *   code    — 业务状态码（20000 成功，40000+ 客户端错误，50000+ 服务端错误）
 *   message — 提示信息
 *   data    — 实际数据，泛型 T，错误时为 null
 *
 * 对外接口：
 *   ApiResponse.success(T data)       — 20000 + "success"
 *   ApiResponse.error(int code, String msg) — 自定义错误码 + 消息
 */
