package com.example.backend.user.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.CertStatus;
import com.example.backend.common.enums.CreditRecordType;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.dto.CreditAppealDto;
import com.example.backend.user.dto.CreditRecordDto;
import com.example.backend.user.entity.CreditAppeal;
import com.example.backend.user.entity.CreditRecord;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.CreditAppealRepository;
import com.example.backend.user.repository.CreditRecordRepository;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CreditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditServiceImpl implements CreditService {

    private static final int MIN_CREDIT_SCORE = 0;
    private static final int MAX_CREDIT_SCORE = 100;
    private static final int ACCEPT_THRESHOLD = 60;

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
        return getCreditScore(userId) >= ACCEPT_THRESHOLD;
    }

    @Override
    public int getCreditScore(Long userId) {
        User user = requireUser(userId);
        return user.getCreditScore();
    }

    @Override
    @Transactional
    public CreditRecordDto addCredit(Long userId, int amount, String reason, Long relatedOrderId) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "加分值必须大于0");
        }
        return adjustCredit(userId, amount, inferAddType(reason, relatedOrderId), reason, relatedOrderId, null);
    }

    @Override
    @Transactional
    public CreditRecordDto deductCredit(Long userId, int amount, String reason, Long relatedOrderId) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "扣分值必须大于0");
        }
        return adjustCredit(userId, -amount, inferDeductType(reason, relatedOrderId), reason, relatedOrderId, null);
    }

    @Override
    @Transactional
    public CreditRecordDto adjustCreditByAdmin(Long userId, int amount, String reason, Long adminId) {
        if (amount == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "调整值不能为0");
        }
        return adjustCredit(userId, amount, CreditRecordType.ADMIN_ADJUST,
                isBlank(reason) ? "管理员调整信用分" : reason, null, adminId);
    }

    @Override
    public List<CreditRecordDto> listRecords(Long userId) {
        return creditRecordRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(CreditRecordDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CreditAppealDto submitAppeal(Long userId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "申诉理由不能为空");
        }
        if (getCreditScore(userId) >= ACCEPT_THRESHOLD) {
            throw new BusinessException(ErrorCode.CONFLICT, "信用分不低于60，无需申诉");
        }
        if (creditAppealRepository.findPendingByUserId(userId).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT, "已有待处理的申诉，请等待审核");
        }
        CreditAppeal appeal = new CreditAppeal();
        appeal.setUserId(userId);
        appeal.setReason(reason.trim());
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
        if (appeal == null) {
            return null;
        }
        if (appeal.getStatus() == CertStatus.PENDING) {
            return CreditAppealDto.from(appeal);
        }
        java.time.LocalDateTime resolvedAt = appeal.getReviewedAt();
        if (resolvedAt == null) {
            return CreditAppealDto.from(appeal);
        }
        boolean hasCreditChangeAfter = creditRecordRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .anyMatch(record -> record.getCreatedAt() != null && record.getCreatedAt().isAfter(resolvedAt));
        return hasCreditChangeAfter ? null : CreditAppealDto.from(appeal);
    }

    @Override
    @Transactional
    public CreditAppealDto reviewAppeal(Long appealId, Long adminId, boolean approved) {
        CreditAppeal appeal = creditAppealRepository.findById(appealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申诉不存在"));
        if (appeal.getStatus() != CertStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "申诉已处理");
        }
        if (approved) {
            appeal.setStatus(CertStatus.APPROVED);
            User user = requireUser(appeal.getUserId());
            int current = user.getCreditScore();
            if (current < ACCEPT_THRESHOLD) {
                adjustCredit(appeal.getUserId(), ACCEPT_THRESHOLD - current,
                        CreditRecordType.APPEAL_RESTORE, "申诉通过，信用分恢复至60", null, adminId);
            }
            notificationService.createNotification(appeal.getUserId(), NotificationType.CREDIT_CHANGED,
                    "信用分申诉已通过", "管理员审核通过，信用分已恢复至60分。", null);
        } else {
            appeal.setStatus(CertStatus.REJECTED);
            notificationService.createNotification(appeal.getUserId(), NotificationType.SYSTEM_ANNOUNCEMENT,
                    "信用分申诉被驳回", "管理员驳回了您的信用分恢复申诉，如有疑问请联系管理员。", null);
        }
        appeal.setReviewedBy(adminId);
        appeal.setReviewedAt(java.time.LocalDateTime.now());
        creditAppealRepository.save(appeal);
        return CreditAppealDto.from(appeal);
    }

    private CreditRecordDto adjustCredit(Long userId, int delta, CreditRecordType type,
                                         String reason, Long relatedOrderId, Long operatorId) {
        User user = requireUser(userId);
        int before = user.getCreditScore();
        int after = clamp(before + delta);
        int actualChange = after - before;
        if (actualChange == 0) {
            return null;
        }
        user.setCreditScore(after);
        userRepository.save(user);

        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setType(type == null ? CreditRecordType.OTHER : type);
        record.setChangeAmount(actualChange);
        record.setBeforeScore(before);
        record.setAfterScore(after);
        record.setReason(isBlank(reason) ? defaultReason(actualChange) : reason);
        record.setRelatedOrderId(relatedOrderId);
        record.setOperatorId(operatorId);
        creditRecordRepository.save(record);

        notificationService.createNotification(userId, NotificationType.CREDIT_CHANGED,
                actualChange > 0 ? "信用分增加 +" + actualChange : "信用分扣除 " + Math.abs(actualChange),
                record.getReason(), relatedOrderId);
        return CreditRecordDto.from(record);
    }

    private CreditRecordType inferAddType(String reason, Long relatedOrderId) {
        if (relatedOrderId != null && contains(reason, "订单完成")) {
            return CreditRecordType.ORDER_REWARD;
        }
        return CreditRecordType.OTHER;
    }

    private CreditRecordType inferDeductType(String reason, Long relatedOrderId) {
        if (relatedOrderId != null && contains(reason, "取消")) {
            return CreditRecordType.ORDER_CANCEL_PENALTY;
        }
        if (contains(reason, "仲裁") || contains(reason, "恶意")) {
            return CreditRecordType.ARBITRATION_PENALTY;
        }
        return CreditRecordType.OTHER;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private int clamp(int value) {
        return Math.max(MIN_CREDIT_SCORE, Math.min(MAX_CREDIT_SCORE, value));
    }

    private String defaultReason(int actualChange) {
        return actualChange > 0 ? "信用分增加" : "信用分扣除";
    }

    private boolean contains(String value, String keyword) {
        return value != null && keyword != null && value.contains(keyword);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
