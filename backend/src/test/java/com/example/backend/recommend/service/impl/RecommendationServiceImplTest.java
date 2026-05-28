package com.example.backend.recommend.service.impl;

import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.RewardType;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.demand.dto.DemandDto;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.pattern.matcher.CompositeDemandMatcher;
import com.example.backend.demand.pattern.matcher.CreditScoreMatcher;
import com.example.backend.demand.pattern.matcher.LocationMatcher;
import com.example.backend.demand.pattern.matcher.SkillMatcher;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.recommend.service.RecommendationService;
import com.example.backend.user.dto.UserPublicDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class RecommendationServiceImplTest {

    @Test
    void shouldRankAcceptorsByCompositeMatcherAndFilterUnavailableUsers() {
        UserRepository userRepository = new UserRepository();
        DemandRepository demandRepository = new DemandRepository();

        User publisher = userRepository.findById(1L).orElseThrow(AssertionError::new);
        publisher.setStatus(UserStatus.ACTIVE);
        userRepository.save(publisher);

        User javaHelper = buildUser("java-helper", "Library Java Tutor", "java-helper@library.edu", 95, UserStatus.ACTIVE);
        userRepository.save(javaHelper);
        User lowCredit = buildUser("java-low", "Java Low Credit", "java-low@library.edu", 40, UserStatus.ACTIVE);
        userRepository.save(lowCredit);
        User banned = buildUser("java-banned", "Java Banned", "java-banned@library.edu", 100, UserStatus.BANNED);
        userRepository.save(banned);

        Demand demand = buildDemand(publisher.getId(), "Java Spring help", DemandCategory.SKILL, "library", 50);
        demandRepository.save(demand);

        RecommendationService service = new RecommendationServiceImpl(userRepository, demandRepository);
        List<UserPublicDto> acceptors = service.recommendAcceptors(demand.getId(), 5);

        Assertions.assertTrue(acceptors.size() >= 1);
        Assertions.assertTrue(acceptors.stream().anyMatch(a -> a.getId().equals(javaHelper.getId())));
    }

    @Test
    void shouldRecommendOpenDemandsForUser() {
        UserRepository userRepository = new UserRepository();
        DemandRepository demandRepository = new DemandRepository();
        User user = userRepository.findById(1L).orElseThrow(AssertionError::new);
        user.setUsername("java-helper");
        user.setRealName("Library Java Tutor");
        user.setEmail("java-helper@library.edu");
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        User publisher = userRepository.findById(2L).orElseThrow(AssertionError::new);
        publisher.setRole(UserRole.STUDENT);
        publisher.setStatus(UserStatus.ACTIVE);
        userRepository.save(publisher);

        Demand skillDemand = buildDemand(publisher.getId(), "Java Spring help", DemandCategory.SKILL, "library", 80);
        demandRepository.save(skillDemand);
        Demand closedDemand = buildDemand(publisher.getId(), "Old Java task", DemandCategory.SKILL, "library", 100);
        closedDemand.setStatus(DemandStatus.ACCEPTED);
        demandRepository.save(closedDemand);

        RecommendationService service = new RecommendationServiceImpl(userRepository, demandRepository);
        List<DemandDto> demands = service.recommendDemands(user.getId(), 10);

        Assertions.assertEquals(1, demands.size());
        Assertions.assertEquals(skillDemand.getId(), demands.get(0).getId());
        Assertions.assertNotNull(demands.get(0).getPublisher());
    }

    @Test
    void compositeMatcherShouldProduceStableTopN() {
        CompositeDemandMatcher matcher = new CompositeDemandMatcher();
        matcher.addMatcher(new LocationMatcher(), 0.35);
        matcher.addMatcher(new SkillMatcher(), 0.35);
        matcher.addMatcher(new CreditScoreMatcher(), 0.30);

        Demand demand = buildDemand(1L, "Python tutoring", DemandCategory.SKILL, "library", 30);
        User best = buildUser("python-library", "Python Library Tutor", "best@library.edu", 95, UserStatus.ACTIVE);
        best.setId(10L);
        User second = buildUser("generic", "Generic Helper", "generic@example.edu", 100, UserStatus.ACTIVE);
        second.setId(11L);

        List<User> ranked = matcher.match(demand, Arrays.asList(second, best), 1);

        Assertions.assertEquals(1, ranked.size());
        Assertions.assertEquals(best.getId(), ranked.get(0).getId());
    }

    private User buildUser(String username, String realName, String email, int creditScore, UserStatus status) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setStudentId(username + "-sid");
        user.setRealName(realName);
        user.setEmail(email);
        user.setCreditScore(creditScore);
        user.setRole(UserRole.STUDENT);
        user.setStatus(status);
        return user;
    }

    private Demand buildDemand(Long publisherId, String title, DemandCategory category, String location, int reward) {
        Demand demand = new Demand();
        demand.setPublisherId(publisherId);
        demand.setTitle(title);
        demand.setDescription(title + " description");
        demand.setCategory(category);
        demand.setLocationTag(location);
        demand.setRewardAmount(reward);
        demand.setRewardType(RewardType.MONEY);
        demand.setStatus(DemandStatus.OPEN);
        return demand;
    }
}
