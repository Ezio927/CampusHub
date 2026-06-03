package com.example.backend.demand.service.impl;

import com.example.backend.common.enums.DemandCategory;
import com.example.backend.demand.entity.Demand;
import com.example.backend.user.entity.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

class DemandScorer {

    private static final double W_CATEGORY = 0.40;
    private static final double W_LOCATION = 0.30;
    private static final double W_CREDIT = 0.10;
    private static final double W_FRESHNESS = 0.20;

    private final Map<DemandCategory, Double> categoryPref;
    private final String userCampus;
    private final int userCredit;

    DemandScorer(Map<DemandCategory, Double> categoryPref, User user) {
        this.categoryPref = categoryPref;
        this.userCampus = user != null ? normalize(user.getCampus()) : null;
        this.userCredit = user != null ? user.getCreditScore() : 0;
    }

    double score(Demand demand) {
        if (demand == null) return 0;
        return categoryScore(demand) * W_CATEGORY
                + locationScore(demand) * W_LOCATION
                + creditScore(demand) * W_CREDIT
                + freshnessScore(demand) * W_FRESHNESS;
    }

    private double categoryScore(Demand demand) {
        DemandCategory cat = demand.getCategory();
        if (cat == null || categoryPref.isEmpty()) return 50;
        Double pref = categoryPref.get(cat);
        if (pref == null) return 50;
        double max = categoryPref.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
        return max > 0 ? (pref / max) * 100 : 50;
    }

    private double locationScore(Demand demand) {
        String demandLoc = normalize(demand.getLocationTag());
        if (isBlank(demandLoc) && isBlank(userCampus)) return 50;
        if (isBlank(demandLoc) || isBlank(userCampus)) return 50;
        if (demandLoc.equals(userCampus)) return 100;
        for (String token : demandLoc.split(" ")) {
            if (token.length() >= 2 && userCampus.contains(token)) return 80;
        }
        return 20;
    }

    private double creditScore(Demand demand) {
        int base = userCredit >= 80 ? 60 : (userCredit >= 60 ? 60 : 40);
        if (userCredit < 80) return base;
        int reward = demand.getRewardAmount() != null ? demand.getRewardAmount() : 0;
        int tier = reward <= 10 ? 0 : (reward <= 30 ? 10 : 20);
        return Math.min(100, base + tier);
    }

    private double freshnessScore(Demand demand) {
        LocalDateTime createdAt = demand.getCreatedAt();
        if (createdAt == null) return 50;
        long hours = Duration.between(createdAt, LocalDateTime.now()).toHours();
        if (hours < 1) return 100;
        if (hours < 6) return 95;
        if (hours < 24) return 85;
        if (hours < 72) return 70;
        if (hours < 168) return 50;
        return 25;
    }

    static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static String normalize(String value) {
        return isBlank(value) ? null : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
