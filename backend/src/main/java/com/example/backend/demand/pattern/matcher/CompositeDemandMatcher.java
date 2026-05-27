package com.example.backend.demand.pattern.matcher;

import com.example.backend.demand.entity.Demand;
import com.example.backend.user.entity.User;

import java.util.ArrayList;
import java.util.List;

public class CompositeDemandMatcher implements DemandMatcher {

    private final List<DemandMatcher> matchers = new ArrayList<DemandMatcher>();

    public void addMatcher(DemandMatcher matcher) {
        if (matcher != null) {
            matchers.add(matcher);
        }
    }

    @Override
    public List<User> match(Demand demand) {
        return new ArrayList<User>();
    }

    @Override
    public double score(User user, Demand demand) {
        double total = 0;
        for (DemandMatcher matcher : matchers) {
            total += matcher.score(user, demand);
        }
        return total;
    }
}
