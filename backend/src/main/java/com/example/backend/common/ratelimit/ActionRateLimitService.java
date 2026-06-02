package com.example.backend.common.ratelimit;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActionRateLimitService {

    private final RateLimitProperties properties;
    private final Map<String, UserRateState> states = new ConcurrentHashMap<>();

    public ActionRateLimitService(RateLimitProperties properties) {
        this.properties = properties;
    }

    public void checkAndRecord(Long userId, RateLimitAction action) {
        if (userId == null || action == null) {
            return;
        }
        RateLimitRule rule = properties.ruleFor(action);
        if (rule == null || isDisabled(rule)) {
            return;
        }
        String key = userId + ":" + action.name();
        Instant now = Instant.now();
        UserRateState state = states.computeIfAbsent(key, ignored -> new UserRateState());
        synchronized (state) {
            prune(state.timestamps, now);
            if (rule.getMinIntervalSeconds() > 0 && state.lastAt != null) {
                long elapsed = now.toEpochMilli() - state.lastAt.toEpochMilli();
                long required = rule.getMinIntervalSeconds() * 1000L;
                if (elapsed < required) {
                    long waitSeconds = (required - elapsed + 999) / 1000;
                    throw tooFrequent(actionLabel(action) + "过于频繁，请 " + waitSeconds + " 秒后再试");
                }
            }
            if (rule.getMaxPerHour() > 0 && countSince(state.timestamps, now.minusSeconds(3600)) >= rule.getMaxPerHour()) {
                throw tooFrequent(actionLabel(action) + "已达每小时上限，请稍后再试");
            }
            if (rule.getMaxPerDay() > 0 && countSince(state.timestamps, now.minusSeconds(86400)) >= rule.getMaxPerDay()) {
                throw tooFrequent(actionLabel(action) + "已达每日上限，请明天再试");
            }
            state.lastAt = now;
            state.timestamps.add(now);
        }
    }

    private static boolean isDisabled(RateLimitRule rule) {
        return rule.getMinIntervalSeconds() <= 0
                && rule.getMaxPerHour() <= 0
                && rule.getMaxPerDay() <= 0;
    }

    private static void prune(List<Instant> timestamps, Instant now) {
        Instant cutoff = now.minusSeconds(86400);
        timestamps.removeIf(ts -> ts.isBefore(cutoff));
    }

    private static int countSince(List<Instant> timestamps, Instant since) {
        int count = 0;
        for (Instant ts : timestamps) {
            if (!ts.isBefore(since)) {
                count++;
            }
        }
        return count;
    }

    private static String actionLabel(RateLimitAction action) {
        return switch (action) {
            case DEMAND_CREATE -> "发布需求";
            case MESSAGE_SEND -> "发送私信";
            case MESSAGE_REPORT -> "提交举报";
            case ORDER_ACCEPT -> "接单";
            case ARBITRATION_CREATE -> "发起仲裁";
        };
    }

    private static BusinessException tooFrequent(String message) {
        return new BusinessException(ErrorCode.RATE_LIMITED, message);
    }

    private static final class UserRateState {
        private Instant lastAt;
        private final List<Instant> timestamps = new ArrayList<>();
    }
}
