package com.example.backend.user.service;

import com.example.backend.common.enums.RewardType;
import com.example.backend.user.dto.QuotaAccountDto;
import com.example.backend.user.dto.QuotaRecordDto;

import java.util.List;

public interface QuotaService {

    int CERTIFICATION_REWARD = 20;
    int CASH_REFERENCE_RATIO = 10;
    int DAILY_CHECK_IN_REWARD = 3;
    int PHONE_BIND_REWARD = 10;
    int EMAIL_BIND_REWARD = 10;
    int GOOD_REVIEW_REWARD = 5;
    int DAILY_ORDER_REWARD = 10;
    int LOW_CREDIT_DEPOSIT = 10;
    int LATE_CANCEL_PENALTY = 5;

    QuotaAccountDto getAccount(Long userId);

    List<QuotaRecordDto> listRecords(Long userId);

    QuotaRecordDto checkIn(Long userId);

    QuotaRecordDto grantCertificationReward(Long userId, Long operatorId);

    QuotaRecordDto grantPhoneReward(Long userId);

    QuotaRecordDto grantEmailReward(Long userId);

    QuotaRecordDto rewardGoodReview(Long userId, Long orderId);

    QuotaRecordDto rewardDailyOrderCompletion(Long userId, Long orderId);

    QuotaRecordDto adjustByAdmin(Long userId, int amount, String reason, Long adminId);

    QuotaRecordDto freezeDemandReward(Long userId, Long demandId, RewardType rewardType, Integer rewardAmount);

    void adjustDemandRewardFreeze(Long userId, Long demandId,
                                  RewardType oldType, Integer oldAmount,
                                  RewardType newType, Integer newAmount);

    QuotaRecordDto refundDemandReward(Long userId, Long demandId, RewardType rewardType, Integer rewardAmount, String reason);

    void payDemandReward(Long publisherId, Long acceptorId, Long demandId, Long orderId,
                         RewardType rewardType, Integer rewardAmount);

    QuotaRecordDto freezeOrderDeposit(Long userId, Long orderId, Integer amount);

    QuotaRecordDto refundOrderDeposit(Long userId, Long orderId, Integer amount, String reason);

    QuotaRecordDto deductOrderDeposit(Long userId, Long orderId, Integer amount, String reason);

    QuotaRecordDto deductLateCancelPenalty(Long userId, Long orderId, Integer amount, String reason);
}
