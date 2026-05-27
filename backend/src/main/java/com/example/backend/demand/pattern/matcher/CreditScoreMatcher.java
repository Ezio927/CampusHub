package com.example.backend.demand.pattern.matcher;

import com.example.backend.demand.entity.Demand;
import com.example.backend.user.entity.User;

import java.util.Collections;
import java.util.List;

public class CreditScoreMatcher implements DemandMatcher {

    @Override
    public List<User> match(Demand demand) {
        return Collections.emptyList();
    }

    @Override
    public double score(User user, Demand demand) {
        return user == null ? 0 : user.getCreditScore();
    }
}
