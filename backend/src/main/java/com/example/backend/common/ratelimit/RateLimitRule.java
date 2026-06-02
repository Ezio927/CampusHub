package com.example.backend.common.ratelimit;

public class RateLimitRule {

    private int minIntervalSeconds = 0;
    private int maxPerHour = 0;
    private int maxPerDay = 0;

    public int getMinIntervalSeconds() {
        return minIntervalSeconds;
    }

    public void setMinIntervalSeconds(int minIntervalSeconds) {
        this.minIntervalSeconds = minIntervalSeconds;
    }

    public int getMaxPerHour() {
        return maxPerHour;
    }

    public void setMaxPerHour(int maxPerHour) {
        this.maxPerHour = maxPerHour;
    }

    public int getMaxPerDay() {
        return maxPerDay;
    }

    public void setMaxPerDay(int maxPerDay) {
        this.maxPerDay = maxPerDay;
    }
}
