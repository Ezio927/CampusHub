package com.example.backend.demand.pattern.matcher;

import com.example.backend.demand.entity.Demand;
import com.example.backend.user.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeDemandMatcher implements DemandMatcher {

    private static final int DEFAULT_LIMIT = 10;

    private final List<WeightedMatcher> matchers = new ArrayList<WeightedMatcher>();
    private List<User> candidates = new ArrayList<User>();
    private int limit = DEFAULT_LIMIT;

    public void addMatcher(DemandMatcher matcher) {
        addMatcher(matcher, 1.0);
    }

    public void addMatcher(DemandMatcher matcher, double weight) {
        if (matcher != null) {
            matchers.add(new WeightedMatcher(matcher, weight <= 0 ? 1.0 : weight));
        }
    }

    @Override
    public List<User> match(Demand demand) {
        return match(demand, candidates, limit);
    }

    public void setCandidates(List<User> candidates) {
        this.candidates = candidates == null ? new ArrayList<User>() : new ArrayList<User>(candidates);
    }

    public void setLimit(int limit) {
        this.limit = limit <= 0 ? DEFAULT_LIMIT : limit;
    }

    // [Human-written] Weighted ranking keeps the matching dimensions independent while producing a stable Top N.
    public List<User> match(Demand demand, List<User> candidates, int limit) {
        if (demand == null || candidates == null || candidates.isEmpty()) {
            return new ArrayList<User>();
        }
        final int actualLimit = limit <= 0 ? DEFAULT_LIMIT : limit;
        return candidates.stream()
                .filter(user -> user != null)
                .sorted((left, right) -> compareUsers(left, right, demand))
                .limit(actualLimit)
                .collect(Collectors.toList());
    }

    @Override
    public double score(User user, Demand demand) {
        if (user == null || demand == null) {
            return 0;
        }
        if (matchers.isEmpty()) {
            return 0;
        }
        double total = 0;
        double totalWeight = 0;
        for (WeightedMatcher matcher : matchers) {
            total += matcher.matcher.score(user, demand) * matcher.weight;
            totalWeight += matcher.weight;
        }
        return totalWeight == 0 ? 0 : total / totalWeight;
    }

    private int compareUsers(User left, User right, Demand demand) {
        int byScore = Double.compare(score(right, demand), score(left, demand));
        if (byScore != 0) {
            return byScore;
        }
        int byCredit = Integer.compare(safeCredit(right), safeCredit(left));
        if (byCredit != 0) {
            return byCredit;
        }
        return Long.compare(safeId(left), safeId(right));
    }

    private int safeCredit(User user) {
        return user == null || user.getCreditScore() == null ? 0 : user.getCreditScore();
    }

    private long safeId(User user) {
        return user == null || user.getId() == null ? Long.MAX_VALUE : user.getId();
    }

    private static final class WeightedMatcher {
        private final DemandMatcher matcher;
        private final double weight;

        private WeightedMatcher(DemandMatcher matcher, double weight) {
            this.matcher = matcher;
            this.weight = weight;
        }
    }
}
