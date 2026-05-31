package com.example.backend.user.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.CertStatus;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.user.dto.CreditAppealDto;
import com.example.backend.user.dto.CreditRecordDto;
import com.example.backend.user.entity.CreditAppeal;
import com.example.backend.user.entity.CreditRecord;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.CreditAppealRepository;
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
    private final CreditAppealRepository creditAppealRepository;
    private final NotificationService notificationService;

    public CreditServiceImpl(UserRepository userRepository,
                             CreditRecordRepository creditRecordRepository,
                             CreditAppealRepository creditAppealRepository,
                             NotificationService notificationService) {
        this.userRepository = userRepository;
        this.creditRecordRepository = creditRecordRepository;
        this.creditAppealRepository = creditAppealRepository;
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
                "信用分增加 +" + amount, record.getReason(), relatedOrderId);
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
                "信用分扣除 " + amount, record.getReason(), relatedOrderId);
        return CreditRecordDto.from(record);
    }

    @Override
    public List<CreditRecordDto> listRecords(Long userId) {
        return creditRecordRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(CreditRecordDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public CreditAppealDto submitAppeal(Long userId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "申诉理由不能为空");
        }
        if (getCreditScore(userId) >= 60) {
            throw new BusinessException(ErrorCode.CONFLICT, "信用分不低于60，无需申诉");
        }
        if (creditAppealRepository.findPendingByUserId(userId).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT, "已有待处理的申诉，请等待审核");
        }
        CreditAppeal appeal = new CreditAppeal();
        appeal.setUserId(userId);
        appeal.setReason(reason);
        appeal.setStatus(CertStatus.PENDING);
        creditAppealRepository.save(appeal);
        return CreditAppealDto.from(appeal);
    }

    @Override
    public List<CreditAppealDto> listPendingAppeals() {
        return creditAppealRepository.findByStatusOrderByCreatedAtAsc(CertStatus.PENDING)
                .stream()
                .map(CreditAppealDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public CreditAppealDto getMyLatestAppeal(Long userId) {
        CreditAppeal appeal = creditAppealRepository.findLatestByUserId(userId).orElse(null);
        if (appeal == null) return null;
        if (appeal.getStatus() == CertStatus.PENDING) return CreditAppealDto.from(appeal);
        java.time.LocalDateTime resolvedAt = appeal.getReviewedAt();
        if (resolvedAt == null) return CreditAppealDto.from(appeal);
        boolean hasCreditChangeAfter = creditRecordRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .anyMatch(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(resolvedAt));
        return hasCreditChangeAfter ? null : CreditAppealDto.from(appeal);
    }

    @Override
    public CreditAppealDto reviewAppeal(Long appealId, Long adminId, boolean approved) {
        CreditAppeal appeal = creditAppealRepository.findById(appealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));
        if (appeal.getStatus() != CertStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "申诉已处理");
        }
        if (approved) {
            appeal.setStatus(CertStatus.APPROVED);
            User user = userRepository.findById(appeal.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            int current = user.getCreditScore();
            if (current < 60) {
                int restore = 60 - current;
                user.addCredit(restore);
                userRepository.save(user);
                CreditRecord record = new CreditRecord();
                record.setUserId(appeal.getUserId());
                record.setChangeAmount(restore);
                record.setReason("申诉恢复至60分");
                creditRecordRepository.save(record);
                notificationService.createNotification(appeal.getUserId(), NotificationType.CREDIT_CHANGED,
                        "信用分恢复 +" + restore, "管理员审核通过，信用分已恢复至60分", null);
            }
        } else {
            appeal.setStatus(CertStatus.REJECTED);
            notificationService.createNotification(appeal.getUserId(), NotificationType.SYSTEM_ANNOUNCEMENT,
                    "信用分申诉被驳回", "管理员驳回了您的信用分恢复申请，如有疑问请联系管理员。", null);
        }
        appeal.setReviewedBy(adminId);
        appeal.setReviewedAt(java.time.LocalDateTime.now());
        creditAppealRepository.save(appeal);
        return CreditAppealDto.from(appeal);
    }
}
