/**
 * 全局异常处理器
 *
 * 职责：通过 @ControllerAdvice 拦截所有未捕获异常，统一转为 ApiResponse 格式返回，
 *       避免 Tomcat 默认的 500 错误页或堆栈信息泄露到前端
 *
 * 拦截范围：
 *   BusinessException      → 取其 ErrorCode 和 message，返回对应 code
 *   MethodArgumentNotValidException → 40001 参数校验失败
 *   Exception（兜底）       → 50000 "服务器内部错误"，日志记录完整堆栈
 *
 * 待完成：
 *   - 打印异常日志（含 traceId，方便排查）
 *   - 生产环境不返回详细 error message
 */
