package com.example.backend.user.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.user.dto.CreditRecordDto;
import com.example.backend.user.entity.CreditRecord;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.CreditRecordRepository;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CreditService;
import com.example.backend.notification.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditServiceImpl implements CreditService {

    private final UserRepository userRepository;
    private final CreditRecordRepository creditRecordRepository;
    private final NotificationService notificationService;

    public CreditServiceImpl(UserRepository userRepository,
                             CreditRecordRepository creditRecordRepository,
                             NotificationService notificationService) {
        this.userRepository = userRepository;
        this.creditRecordRepository = creditRecordRepository;
        this.notificationService = notificationService;
    }

    @Override
    public boolean checkCredit(Long userId) {
        return getCreditScore(userId) >= 60;
    }

    @Override
    public int getCreditScore(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getCreditScore();
    }

    @Override
    public CreditRecordDto addCredit(Long userId, int amount, String reason, Long relatedOrderId) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "加分值必须大于0");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.addCredit(amount);
        userRepository.save(user);

        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeAmount(amount);
        record.setReason(reason == null ? "credit increased" : reason);
        record.setRelatedOrderId(relatedOrderId);
        creditRecordRepository.save(record);
        notificationService.createNotification(userId, NotificationType.CREDIT_CHANGED,
                "Credit increased", record.getReason(), relatedOrderId);
        return CreditRecordDto.from(record);
    }

    @Override
    public CreditRecordDto deductCredit(Long userId, int amount, String reason, Long relatedOrderId) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "扣分值必须大于0");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.deductCredit(amount);
        userRepository.save(user);

        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeAmount(-Math.abs(amount));
        record.setReason(reason == null ? "credit deducted" : reason);
        record.setRelatedOrderId(relatedOrderId);
        creditRecordRepository.save(record);
        notificationService.createNotification(userId, NotificationType.CREDIT_CHANGED,
                "Credit deducted", record.getReason(), relatedOrderId);
        return CreditRecordDto.from(record);
    }

    @Override
    public List<CreditRecordDto> listRecords(Long userId) {
        return creditRecordRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(CreditRecordDto::from)
                .collect(Collectors.toList());
    }
}
