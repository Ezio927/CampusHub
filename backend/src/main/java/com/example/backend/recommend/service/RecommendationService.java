package com.example.backend.recommend.service;

import com.example.backend.demand.dto.DemandDto;
import com.example.backend.user.dto.UserPublicDto;

import java.util.List;

public interface RecommendationService {

    List<DemandDto> recommendDemands(Long userId);

    List<DemandDto> recommendDemands(Long userId, int limit);

    List<UserPublicDto> recommendAcceptors(Long demandId);

    List<UserPublicDto> recommendAcceptors(Long demandId, int limit);
}
