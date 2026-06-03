package com.example.backend.common.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private RateLimitRule demandCreate = defaultDemandCreate();
    private RateLimitRule messageSend = defaultMessageSend();
    private RateLimitRule messageReport = defaultMessageReport();
    private RateLimitRule orderAccept = defaultOrderAccept();
    private RateLimitRule arbitrationCreate = defaultArbitrationCreate();

    public RateLimitRule ruleFor(RateLimitAction action) {
        return switch (action) {
            case DEMAND_CREATE -> demandCreate;
            case MESSAGE_SEND -> messageSend;
            case MESSAGE_REPORT -> messageReport;
            case ORDER_ACCEPT -> orderAccept;
            case ARBITRATION_CREATE -> arbitrationCreate;
        };
    }

    public RateLimitRule getDemandCreate() {
        return demandCreate;
    }

    public void setDemandCreate(RateLimitRule demandCreate) {
        this.demandCreate = demandCreate;
    }

    public RateLimitRule getMessageSend() {
        return messageSend;
    }

    public void setMessageSend(RateLimitRule messageSend) {
        this.messageSend = messageSend;
    }

    public RateLimitRule getMessageReport() {
        return messageReport;
    }

    public void setMessageReport(RateLimitRule messageReport) {
        this.messageReport = messageReport;
    }

    public RateLimitRule getOrderAccept() {
        return orderAccept;
    }

    public void setOrderAccept(RateLimitRule orderAccept) {
        this.orderAccept = orderAccept;
    }

    public RateLimitRule getArbitrationCreate() {
        return arbitrationCreate;
    }

    public void setArbitrationCreate(RateLimitRule arbitrationCreate) {
        this.arbitrationCreate = arbitrationCreate;
    }

    private static RateLimitRule defaultDemandCreate() {
        RateLimitRule rule = new RateLimitRule();
        rule.setMinIntervalSeconds(10);
        rule.setMaxPerHour(10);
        rule.setMaxPerDay(30);
        return rule;
    }

    private static RateLimitRule defaultMessageSend() {
        RateLimitRule rule = new RateLimitRule();
        rule.setMinIntervalSeconds(2);
        rule.setMaxPerHour(120);
        rule.setMaxPerDay(500);
        return rule;
    }

    private static RateLimitRule defaultMessageReport() {
        RateLimitRule rule = new RateLimitRule();
        rule.setMinIntervalSeconds(30);
        rule.setMaxPerHour(5);
        rule.setMaxPerDay(10);
        return rule;
    }

    private static RateLimitRule defaultOrderAccept() {
        RateLimitRule rule = new RateLimitRule();
        rule.setMinIntervalSeconds(5);
        rule.setMaxPerHour(20);
        rule.setMaxPerDay(50);
        return rule;
    }

    private static RateLimitRule defaultArbitrationCreate() {
        RateLimitRule rule = new RateLimitRule();
        rule.setMinIntervalSeconds(60);
        rule.setMaxPerHour(3);
        rule.setMaxPerDay(5);
        return rule;
    }
}
