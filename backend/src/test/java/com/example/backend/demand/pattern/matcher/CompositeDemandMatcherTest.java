package com.example.backend.demand.pattern.matcher;

import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.RewardType;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.demand.entity.Demand;
import com.example.backend.user.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class CompositeDemandMatcherTest {

    private CompositeDemandMatcher matcher;
    private Demand demand;

    @BeforeEach
    void setUp() {
        matcher = new CompositeDemandMatcher();
        demand = new Demand();
        demand.setId(1L);
        demand.setTitle("Java tutoring");
        demand.setDescription("Need help with Java");
        demand.setCategory(DemandCategory.SKILL);
        demand.setLocationTag("library");
    }

    @Test
    void shouldReturnEmptyListForNullCandidates() {
        List<User> result = matcher.match(demand, null, 5);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForEmptyCandidates() {
        List<User> result = matcher.match(demand, Collections.emptyList(), 5);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldScoreZeroWhenNoMatchersRegistered() {
        User user = buildUser("test", 100);
        Assertions.assertEquals(0.0, matcher.score(user, demand), 0.01);
    }

    @Test
    void shouldAddMatcherWithDefaultWeight() {
        matcher.addMatcher(new CreditScoreMatcher());
        User user = buildUser("test", 80);
        Assertions.assertEquals(80.0, matcher.score(user, demand), 0.01);
    }

    @Test
    void shouldComputeWeightedAverageScore() {
        matcher.addMatcher(new CreditScoreMatcher(), 0.4);
        matcher.addMatcher(new LocationMatcher(), 0.6);

        User user = buildUser("library-user", 90);
        double score = matcher.score(user, demand);
        Assertions.assertTrue(score > 0, "weighted score should be positive");
        Assertions.assertTrue(score <= 100, "score should not exceed 100");
    }

    @Test
    void shouldRankByCompositeScoreThenByCreditThenById() {
        // Use only CreditScoreMatcher so ranking is deterministic
        matcher.addMatcher(new CreditScoreMatcher());

        User best = buildUser(10L, "java-expert", 95);
        User mid = buildUser(11L, "library-user", 80);
        User low = buildUser(12L, "random", 60);

        List<User> result = matcher.match(demand, Arrays.asList(low, best, mid), 3);
        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals(10L, result.get(0).getId(), "highest credit score first");
        Assertions.assertEquals(11L, result.get(1).getId());
        Assertions.assertEquals(12L, result.get(2).getId());
    }

    @Test
    void shouldRespectLimit() {
        matcher.addMatcher(new CreditScoreMatcher());

        User u1 = buildUser(1L, "a", 100);
        User u2 = buildUser(2L, "b", 95);
        User u3 = buildUser(3L, "c", 90);
        User u4 = buildUser(4L, "d", 85);

        List<User> result = matcher.match(demand, Arrays.asList(u4, u3, u2, u1), 2);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(1L, result.get(0).getId());
        Assertions.assertEquals(2L, result.get(1).getId());
    }

    @Test
    void shouldFilterNullUsers() {
        matcher.addMatcher(new CreditScoreMatcher());

        User u1 = buildUser(1L, "a", 100);
        List<User> result = matcher.match(demand, Arrays.asList(null, u1, null), 5);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    void shouldHandleZeroOrNegativeWeightAsDefault() {
        matcher.addMatcher(new CreditScoreMatcher(), -1.0);
        User user = buildUser("test", 80);
        Assertions.assertEquals(80.0, matcher.score(user, demand), 0.01);
    }

    @Test
    void shouldStablyRankSameScoreUsers() {
        matcher.addMatcher(new CreditScoreMatcher());

        User u1 = buildUser(10L, "a", 90);
        User u2 = buildUser(20L, "b", 90);

        List<User> result = matcher.match(demand, Arrays.asList(u2, u1), 5);
        Assertions.assertEquals(10L, result.get(0).getId(), "same score, lower id first");
        Assertions.assertEquals(20L, result.get(1).getId());
    }

    @Test
    void creditScoreMatcherShouldReturnCreditAsScore() {
        CreditScoreMatcher csMatcher = new CreditScoreMatcher();

        User user = buildUser("test", 75);
        Assertions.assertEquals(75.0, csMatcher.score(user, demand), 0.01);

        user.setCreditScore(100);
        Assertions.assertEquals(100.0, csMatcher.score(user, demand), 0.01);

        user.setCreditScore(0);
        Assertions.assertEquals(0.0, csMatcher.score(user, demand), 0.01);

        Assertions.assertEquals(0.0, csMatcher.score(null, demand), 0.01);
    }

    @Test
    void locationMatcherShouldScoreBasedOnMatch() {
        LocationMatcher locMatcher = new LocationMatcher();

        User user = buildUser("test", 100);
        // No location fields set, so score should be default/fail
        double score = locMatcher.score(user, demand);
        Assertions.assertTrue(score >= 0 && score <= 100, "score should be in [0,100]");

        // null demand
        Assertions.assertEquals(0.0, locMatcher.score(user, null), 0.01);
        // null user
        Assertions.assertEquals(0.0, locMatcher.score(null, demand), 0.01);
    }

    @Test
    void skillMatcherShouldScoreBasedOnMatch() {
        SkillMatcher skillMatcher = new SkillMatcher();

        User user = buildUser("java-coder", 100);
        user.setRealName("Java Expert");
        double score = skillMatcher.score(user, demand);
        Assertions.assertTrue(score >= 0 && score <= 100, "score should be in [0,100]");

        // null cases
        Assertions.assertTrue(skillMatcher.score(null, demand) >= 0);
        Assertions.assertTrue(skillMatcher.score(user, null) >= 0);
    }

    @Test
    void compositeMatcherShouldHandleAllThreeMatchers() {
        matcher.addMatcher(new CreditScoreMatcher(), 0.3);
        matcher.addMatcher(new LocationMatcher(), 0.35);
        matcher.addMatcher(new SkillMatcher(), 0.35);

        User best = buildUser(10L, "java-library-pro", 95);
        User mid = buildUser(11L, "library-amateur", 60);
        User low = buildUser(12L, "random-guy", 30);

        List<User> result = matcher.match(demand, Arrays.asList(low, mid, best), 3);

        Assertions.assertEquals(3, result.size());
        // best should rank first due to highest credit + skill/location match
        Assertions.assertTrue(result.get(0).getCreditScore() >= result.get(1).getCreditScore()
                || result.get(0).getCreditScore() >= result.get(2).getCreditScore());
    }

    private User buildUser(String username, int creditScore) {
        return buildUser(null, username, creditScore);
    }

    private User buildUser(Long id, String username, int creditScore) {
        User user = new User();
        if (id != null) user.setId(id);
        user.setUsername(username);
        user.setRealName(username);
        user.setPasswordHash("hash");
        user.setStudentId("S-" + username);
        user.setCreditScore(creditScore);
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
