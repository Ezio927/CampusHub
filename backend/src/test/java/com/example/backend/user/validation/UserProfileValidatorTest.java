package com.example.backend.user.validation;

import com.example.backend.common.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserProfileValidatorTest {

    @Test
    void usernameShouldAllowChineseEnglishUnderscoreAndHyphen() {
        Assertions.assertEquals("张三_A-09",
                UserProfileValidator.requireValidUsername(" 张三_A-09 "));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.requireValidUsername("a"));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.requireValidUsername("user@nju"));
    }

    @Test
    void realNameShouldOnlyAllowChineseAndEnglish() {
        Assertions.assertEquals("Alice张三", UserProfileValidator.requireValidRealName("Alice张三"));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.requireValidRealName("张_三"));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.requireValidRealName("A"));
    }

    @Test
    void phoneShouldMatchMainlandChinaMobileSegments() {
        Assertions.assertEquals("13812345678", UserProfileValidator.normalizeOptionalPhone("13812345678"));
        Assertions.assertEquals("14912345678", UserProfileValidator.normalizeOptionalPhone("14912345678"));
        Assertions.assertEquals("16712345678", UserProfileValidator.normalizeOptionalPhone("16712345678"));
        Assertions.assertEquals("17812345678", UserProfileValidator.normalizeOptionalPhone("17812345678"));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.normalizeOptionalPhone("14412345678"));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.normalizeOptionalPhone("+8613812345678"));
    }

    @Test
    void emailShouldOnlyAllowConfiguredDomains() {
        Assertions.assertEquals("student@smail.nju.edu.cn",
                UserProfileValidator.normalizeOptionalEmail("Student@SMAIL.NJU.EDU.CN"));
        Assertions.assertEquals("teacher@nju.edu.cn",
                UserProfileValidator.normalizeOptionalEmail("teacher@nju.edu.cn"));
        Assertions.assertEquals("name@qq.com", UserProfileValidator.normalizeOptionalEmail("name@qq.com"));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.normalizeOptionalEmail("name@example.com"));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.normalizeOptionalEmail("bad email@qq.com"));
    }

    @Test
    void studentIdShouldMatchNjuUndergraduateAndGraduateRules() {
        Assertions.assertEquals("201250001", UserProfileValidator.requireValidStudentId("201250001"));
        Assertions.assertEquals("241090001", UserProfileValidator.requireValidStudentId("241090001"));
        Assertions.assertEquals("245500123", UserProfileValidator.requireValidStudentId("245500123"));
        Assertions.assertEquals("502024320001", UserProfileValidator.requireValidStudentId("502024320001"));
        Assertions.assertEquals("MG21320001", UserProfileValidator.requireValidStudentId("mg21320001"));

        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.requireValidStudentId("203250001"));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.requireValidStudentId("201260001"));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.requireValidStudentId("201250000"));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.requireValidStudentId("502021320001"));
        Assertions.assertThrows(BusinessException.class,
                () -> UserProfileValidator.requireValidStudentId("MG22320001"));
    }
}
