package com.example.backend.user.service;

import com.example.backend.user.dto.CreditAppealDto;
import com.example.backend.user.dto.CreditRecordDto;

import java.util.List;

public interface CreditService {

    boolean checkCredit(Long userId);

    int getCreditScore(Long userId);

    CreditRecordDto addCredit(Long userId, int amount, String reason, Long relatedOrderId);

    CreditRecordDto deductCredit(Long userId, int amount, String reason, Long relatedOrderId);

    CreditRecordDto adjustCreditByAdmin(Long userId, int amount, String reason, Long adminId);

    List<CreditRecordDto> listRecords(Long userId);

    CreditAppealDto submitAppeal(Long userId, String reason);

    List<CreditAppealDto> listPendingAppeals();

    CreditAppealDto reviewAppeal(Long appealId, Long adminId, boolean approved);

    CreditAppealDto getMyLatestAppeal(Long userId);
}
