package com.example.backend.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ========== 业务异常 ==========

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(httpStatus(ex.getCode()))
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    private static int httpStatus(int errorCode) {
        if (errorCode >= 50000) return 500;
        return errorCode / 100; // 401xx→401, 403xx→403, 404xx→404, 409xx→409
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ApiResponse<Void>> handleNumberFormat(NumberFormatException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_PARAM.getCode(), "参数格式无效"));
    }

    // ========== 请求参数异常 → 400 ==========

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_PARAM.getCode(),
                        "参数类型错误: " + ex.getName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), "请求体格式错误"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(),
                        "缺少必要参数: " + ex.getParameterName()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_PARAM.getCode(), "图片需小于3MB"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipart(MultipartException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), "图片上传失败，请重新选择图片"));
    }

    // ========== 路由不存在 → 404 ==========

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(404)
                .body(ApiResponse.error(ErrorCode.NOT_FOUND));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unhandled server exception", ex);
        return ResponseEntity.status(500)
                .body(ApiResponse.error(ErrorCode.SERVER_ERROR));
    }
}
