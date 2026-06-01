package com.example.backend.user.validation;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class UserProfileValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[\\p{IsHan}A-Za-z0-9_-]{2,15}$");
    private static final Pattern REAL_NAME_PATTERN = Pattern.compile("^[\\p{IsHan}A-Za-z]{2,15}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(13\\d|14[5-9]|15\\d|16[5-7]|17[0-8]|18\\d|19\\d)\\d{8}$");
    private static final Pattern UNDERGRAD_STUDENT_ID_PATTERN = Pattern.compile("^\\d{9}$");
    private static final Pattern GRADUATE_STUDENT_ID_PATTERN = Pattern.compile("^\\d{12}$");
    private static final Pattern LEGACY_GRADUATE_STUDENT_ID_PATTERN = Pattern.compile("^[A-Z]{2}\\d{8}$");
    private static final Pattern EMAIL_LOCAL_PART_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+$");
    private static final Set<String> GRADUATE_DEGREE_CODES = Set.of("50", "52", "60", "65", "66");
    private static final Set<String> DEPARTMENT_CODES = Set.of(
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
            "21", "22", "23", "24", "25", "27", "28", "29", "30", "32",
            "38", "43", "78");
    private static final Set<String> UNDERGRAD_MAJOR_CATEGORY_CODES = Set.of(
            "098", "108", "180", "230", "232", "234", "240", "250",
            "810", "820", "830", "840", "850", "860", "870", "880", "900");
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
                    "用户名仅支持中英文、数字、下划线和连字符，长度为2-15个字符");
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

    public static String requireValidStudentId(String studentId) {
        String value = normalizeRequired(studentId, "学号不能为空").toUpperCase(Locale.ROOT);
        if (isValidUndergraduateStudentId(value)
                || isValidGraduateStudentId(value)
                || isValidLegacyGraduateStudentId(value)) {
            return value;
        }
        throw new BusinessException(ErrorCode.INVALID_PARAM,
                "学号格式不符合南京大学规则：本科9位数字；2022年起研究生12位数字；2022年前研究生为2位大写字母加8位数字");
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

    private static boolean isValidUndergraduateStudentId(String value) {
        if (!UNDERGRAD_STUDENT_ID_PATTERN.matcher(value).matches()) {
            return false;
        }
        String admissionType = value.substring(2, 3);
        String majorCode = value.substring(3, 6);
        String serial = value.substring(6);
        return Set.of("1", "2", "5").contains(admissionType)
                && isValidUndergraduateMajorCode(majorCode)
                && !"000".equals(serial);
    }

    private static boolean isValidUndergraduateMajorCode(String majorCode) {
        return UNDERGRAD_MAJOR_CATEGORY_CODES.contains(majorCode)
                || majorCode.matches("50\\d")
                || DEPARTMENT_CODES.contains(majorCode.substring(0, 2));
    }

    private static boolean isValidGraduateStudentId(String value) {
        if (!GRADUATE_STUDENT_ID_PATTERN.matcher(value).matches()) {
            return false;
        }
        String degreeCode = value.substring(0, 2);
        int year = Integer.parseInt(value.substring(2, 6));
        String majorCode = value.substring(6, 9);
        String serial = value.substring(9);
        return GRADUATE_DEGREE_CODES.contains(degreeCode)
                && year >= 2022
                && !"000".equals(majorCode)
                && !"000".equals(serial);
    }

    private static boolean isValidLegacyGraduateStudentId(String value) {
        if (!LEGACY_GRADUATE_STUDENT_ID_PATTERN.matcher(value).matches()) {
            return false;
        }
        int year = Integer.parseInt(value.substring(2, 4));
        String majorCode = value.substring(4, 7);
        String serial = value.substring(7);
        return year <= 21
                && !"000".equals(majorCode)
                && !"000".equals(serial);
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
