package com.example.backend.user.validation;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class UserProfileValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[\\p{IsHan}A-Za-z_-]{2,15}$");
    private static final Pattern REAL_NAME_PATTERN = Pattern.compile("^[\\p{IsHan}A-Za-z]{2,15}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(13\\d|14[5-9]|15\\d|16[5-7]|17[0-8]|18\\d|19\\d)\\d{8}$");
    private static final Pattern EMAIL_LOCAL_PART_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+$");
    private static final Set<String> ALLOWED_EMAIL_DOMAINS = Set.of(
            "qq.com",
            "163.com",
            "126.com",
            "yeah.net",
            "gmail.com",
            "outlook.com",
            "hotmail.com",
            "live.com",
            "foxmail.com",
            "sina.com",
            "sina.cn",
            "sohu.com",
            "139.com",
            "189.cn",
            "wo.cn",
            "yahoo.com",
            "ymail.com",
            "icloud.com",
            "smail.nju.edu.cn",
            "nju.edu.cn");

    private UserProfileValidator() {
    }

    public static String requireValidUsername(String username) {
        String value = normalizeRequired(username, "用户名不能为空");
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    "用户名仅支持中英文、下划线和连字符，长度为2-15个字符");
        }
        return value;
    }

    public static String requireValidRealName(String realName) {
        String value = normalizeRequired(realName, "姓名不能为空");
        if (!REAL_NAME_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    "姓名仅支持汉字和英文，长度为2-15个字符");
        }
        return value;
    }

    public static String normalizeOptionalPhone(String phone) {
        String value = normalizeOptional(phone);
        if (value == null) {
            return null;
        }
        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    "手机号必须为中国大陆11位手机号，且号段合法");
        }
        return value;
    }

    public static String normalizeOptionalEmail(String email) {
        String value = normalizeOptional(email);
        if (value == null) {
            return null;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        int at = normalized.lastIndexOf('@');
        if (at <= 0 || at == normalized.length() - 1) {
            throw invalidEmail();
        }
        String localPart = normalized.substring(0, at);
        String domain = normalized.substring(at + 1);
        if (!EMAIL_LOCAL_PART_PATTERN.matcher(localPart).matches()
                || !ALLOWED_EMAIL_DOMAINS.contains(domain)) {
            throw invalidEmail();
        }
        return normalized;
    }

    private static BusinessException invalidEmail() {
        return new BusinessException(ErrorCode.INVALID_PARAM, "邮箱仅支持常见邮箱或南京大学教育邮箱后缀");
    }

    private static String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
