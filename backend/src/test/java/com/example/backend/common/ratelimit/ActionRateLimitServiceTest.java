package com.example.backend.common.ratelimit;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ActionRateLimitServiceTest {

    @Test
    void shouldRejectActionsWithinMinInterval() {
        RateLimitProperties properties = new RateLimitProperties();
        RateLimitRule rule = new RateLimitRule();
        rule.setMinIntervalSeconds(60);
        rule.setMaxPerHour(0);
        rule.setMaxPerDay(0);
        properties.setMessageSend(rule);

        ActionRateLimitService service = new ActionRateLimitService(properties);
        service.checkAndRecord(1L, RateLimitAction.MESSAGE_SEND);

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                () -> service.checkAndRecord(1L, RateLimitAction.MESSAGE_SEND));
        Assertions.assertEquals(ErrorCode.RATE_LIMITED.getCode(), ex.getCode());
        Assertions.assertTrue(ex.getMessage().contains("发送私信"));
    }

    @Test
    void shouldRejectWhenHourlyCapReached() {
        RateLimitProperties properties = new RateLimitProperties();
        RateLimitRule rule = new RateLimitRule();
        rule.setMinIntervalSeconds(0);
        rule.setMaxPerHour(2);
        rule.setMaxPerDay(0);
        properties.setMessageReport(rule);

        ActionRateLimitService service = new ActionRateLimitService(properties);
        service.checkAndRecord(2L, RateLimitAction.MESSAGE_REPORT);
        service.checkAndRecord(2L, RateLimitAction.MESSAGE_REPORT);

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                () -> service.checkAndRecord(2L, RateLimitAction.MESSAGE_REPORT));
        Assertions.assertEquals(ErrorCode.RATE_LIMITED.getCode(), ex.getCode());
        Assertions.assertTrue(ex.getMessage().contains("每小时"));
    }
}
