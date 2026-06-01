package com.example.backend.common.moderation;

import com.example.backend.common.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ContentModerationServiceTest {

    private final ContentModerationService service = new ContentModerationService();

    @Test
    void shouldNormalizeFullWidthCasePunctuationAndZeroWidthCharacters() {
        String normalized = service.normalizeForModeration("Ａ_b-\u200Bc，D");
        Assertions.assertEquals("abcd", normalized);
    }

    @Test
    void usernameShouldBeBlockedWhenSensitive() {
        Assertions.assertThrows(BusinessException.class,
                () -> service.assertUsernameAllowed("擦-边"));
    }

    @Test
    void messageShouldBlockHeavyCategoriesButAllowContactWarning() {
        Assertions.assertThrows(BusinessException.class,
                () -> service.assertMessageAllowed("请扫二维码"));

        ModerationResult result = service.assertMessageAllowed("我的微信wx1234567");
        Assertions.assertFalse(result.getWarnings().isEmpty());
    }

    @Test
    void demandContentShouldUseSameModerationRules() {
        Assertions.assertThrows(BusinessException.class,
                () -> service.assertDemandContentAllowed("标题", "请看www.example.com"));

        ModerationResult result = service.assertDemandContentAllowed("详细描述", "电话13800138000");
        Assertions.assertFalse(result.getWarnings().isEmpty());
    }
}
