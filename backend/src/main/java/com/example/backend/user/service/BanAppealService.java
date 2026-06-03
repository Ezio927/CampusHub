package com.example.backend.user.service;

import com.example.backend.user.dto.BanAppealDto;

import java.util.List;

public interface BanAppealService {

    BanAppealDto submitAppeal(Long userId, String reason);

    BanAppealDto getMyLatestAppeal(Long userId);

    List<BanAppealDto> listPendingAppeals();

    BanAppealDto reviewAppeal(Long appealId, Long adminId, boolean approved);
}
