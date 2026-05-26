/**
 * 业务异常
 *
 * 职责：业务层遇到预期内的错误（如"信用分不足""需求已被接单"）时抛出此异常，
 *       由 GlobalExceptionHandler 统一捕获并转 ApiResponse
 *
 * 对外接口：
 *   BusinessException(int code, String message)
 *   BusinessException(ErrorCode code)
 */
