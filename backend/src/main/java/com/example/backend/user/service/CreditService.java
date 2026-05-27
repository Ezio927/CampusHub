package com.example.backend.user.service;

import com.example.backend.user.dto.CreditRecordDto;

import java.util.List;

public interface CreditService {

    boolean checkCredit(Long userId);

    int getCreditScore(Long userId);

    CreditRecordDto addCredit(Long userId, int amount, String reason, Long relatedOrderId);

    CreditRecordDto deductCredit(Long userId, int amount, String reason, Long relatedOrderId);

    List<CreditRecordDto> listRecords(Long userId);
}
