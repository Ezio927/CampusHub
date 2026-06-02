package com.example.backend.common.validation;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;

import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ImageDataUrlValidator {

    private static final int MAX_IMAGE_DATA_URL_LENGTH = 1000000;
    private static final Pattern LOCAL_UPLOAD_IMAGE = Pattern.compile(
            "^/uploads/[A-Za-z0-9][A-Za-z0-9._-]*\\.(png|jpg|jpeg|gif|webp)$",
            Pattern.CASE_INSENSITIVE);

    private ImageDataUrlValidator() {
    }

    public static String requireValidImageDataUrl(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + "不能为空");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_IMAGE_DATA_URL_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, fieldName + "过大，请上传700KB以内的图片");
        }
        int comma = normalized.indexOf(',');
        if (comma <= 0 || comma == normalized.length() - 1) {
            throw invalidImage(fieldName);
        }
        String prefix = normalized.substring(0, comma + 1).toLowerCase(Locale.ROOT);
        boolean allowedType = prefix.equals("data:image/png;base64,")
                || prefix.equals("data:image/jpeg;base64,")
                || prefix.equals("data:image/jpg;base64,")
                || prefix.equals("data:image/gif;base64,")
                || prefix.equals("data:image/webp;base64,");
        if (!allowedType) {
            throw invalidImage(fieldName);
        }
        try {
            Base64.getDecoder().decode(normalized.substring(comma + 1));
        } catch (IllegalArgumentException ex) {
            throw invalidImage(fieldName);
        }
        return normalized;
    }

    public static String requireValidImageReference(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + "不能为空");
        }
        String normalized = value.trim();
        if (LOCAL_UPLOAD_IMAGE.matcher(normalized).matches()) {
            return normalized;
        }
        return requireValidImageDataUrl(normalized, fieldName);
    }

    private static BusinessException invalidImage(String fieldName) {
        return new BusinessException(ErrorCode.INVALID_PARAM,
                fieldName + "格式仅支持png、jpg、gif、webp图片");
    }
}
