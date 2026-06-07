package com.example.backend.demand.pattern.matcher;

import com.example.backend.demand.entity.Demand;
import com.example.backend.user.entity.User;

public class CreditScoreMatcher implements DemandMatcher {

    @Override
    public double score(User user, Demand demand) {
        if (user == null || user.getCreditScore() == null) {
            return 0;
        }
        return Math.max(0, Math.min(100, user.getCreditScore()));
    }
}
