package com.example.backend.recommend.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.DemandStatus;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final int DEFAULT_LIMIT = 10;

    private final UserRepository userRepository;
    private final DemandRepository demandRepository;
    private final CompositeDemandMatcher matcher;

    @Autowired
    public RecommendationServiceImpl(UserRepository userRepository, DemandRepository demandRepository) {
        this(userRepository, demandRepository, defaultMatcher());
    }

    public RecommendationServiceImpl(UserRepository userRepository,
                                     DemandRepository demandRepository,
                                     CompositeDemandMatcher matcher) {
        this.userRepository = userRepository;
        this.demandRepository = demandRepository;
        this.matcher = matcher == null ? defaultMatcher() : matcher;
    }

    @Override
    public List<DemandDto> recommendDemands(Long userId) {
        return recommendDemands(userId, DEFAULT_LIMIT);
    }

    @Override
    public List<DemandDto> recommendDemands(Long userId, int limit) {
        User user = requireUser(userId);
        int actualLimit = normalizeLimit(limit);
        return demandRepository.findAll().stream()
                .filter(demand -> demand.getStatus() == DemandStatus.OPEN)
                .filter(demand -> demand.getPublisherId() == null || !demand.getPublisherId().equals(userId))
                .sorted(Comparator
                        .comparingDouble((Demand demand) -> demandScore(user, demand)).reversed()
                        .thenComparing(Demand::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Demand::getId, Comparator.nullsLast(Long::compareTo)))
                .limit(actualLimit)
                .map(this::toDemandDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserPublicDto> recommendAcceptors(Long demandId) {
        return recommendAcceptors(demandId, DEFAULT_LIMIT);
    }

    @Override
    public List<UserPublicDto> recommendAcceptors(Long demandId, int limit) {
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEMAND_NOT_FOUND));
        List<User> candidates = userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.STUDENT)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .filter(User::isCreditSufficient)
                .filter(user -> demand.getPublisherId() == null || !user.getId().equals(demand.getPublisherId()))
                .collect(Collectors.toList());
        return matcher.match(demand, candidates, normalizeLimit(limit)).stream()
                .map(UserPublicDto::from)
                .collect(Collectors.toList());
    }

    private User requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户ID不能为空");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private DemandDto toDemandDto(Demand demand) {
        DemandDto dto = DemandDto.from(demand);
        if (dto != null && demand.getPublisherId() != null) {
            userRepository.findById(demand.getPublisherId()).ifPresent(user -> dto.setPublisher(UserPublicDto.from(user)));
        }
        return dto;
    }

    private double demandScore(User user, Demand demand) {
        double score = matcher.score(user, demand);
        if (Boolean.TRUE.equals(demand.getUrgent())) {
            score += 5;
        }
        if (demand.getRewardAmount() != null) {
            score += Math.min(10, Math.max(0, demand.getRewardAmount()) / 10.0);
        }
        return score;
    }

    private int normalizeLimit(int limit) {
        return limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, 50);
    }

    private static CompositeDemandMatcher defaultMatcher() {
        CompositeDemandMatcher composite = new CompositeDemandMatcher();
        composite.addMatcher(new LocationMatcher(), 0.35);
        composite.addMatcher(new SkillMatcher(), 0.35);
        composite.addMatcher(new CreditScoreMatcher(), 0.30);
        return composite;
    }
}
