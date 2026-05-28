package com.example.backend.user.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.CertStatus;
import com.example.backend.common.enums.CertType;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.user.dto.CertificationDto;
import com.example.backend.user.dto.CertificationSubmitDto;
import com.example.backend.user.entity.Certification;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.CertificationRepository;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CertificationService;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.common.enums.NotificationType;
import org.springframework.stereotype.Service;

@Service
public class CertificationServiceImpl implements CertificationService {

    private final CertificationRepository certificationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CertificationServiceImpl(CertificationRepository certificationRepository,
                                    UserRepository userRepository,
                                    NotificationService notificationService) {
        this.certificationRepository = certificationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public CertificationDto submit(Long userId, CertificationSubmitDto req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException(ErrorCode.ACCOUNT_BANNED);
        }
        if (req == null || req.getCertType() == null || isBlank(req.getStudentId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "认证类型和学号不能为空");
        }
        if (req.getCertType() == CertType.STUDENT_CARD && isBlank(req.getCardImageUrl())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校园卡认证需要上传图片");
        }

        certificationRepository.findByUserId(userId).ifPresent(existing -> {
            if (existing.getStatus() == CertStatus.PENDING) {
                throw new BusinessException(ErrorCode.CERTIFICATION_PENDING_EXISTS);
            }
            if (existing.getStatus() == CertStatus.APPROVED) {
                throw new BusinessException(ErrorCode.CERTIFICATION_ALREADY_APPROVED);
            }
        });

        Certification certification = certificationRepository.findByUserId(userId).orElseGet(Certification::new);
        certification.setUserId(userId);
        certification.setCertType(req.getCertType());
        certification.setStudentId(req.getStudentId());
        certification.setCardImageUrl(req.getCardImageUrl());
        certification.setStatus(CertStatus.PENDING);
        certification.setRejectReason(null);
        certification.setReviewedBy(null);
        certification.setReviewedAt(null);
        certificationRepository.save(certification);

        user.setStatus(UserStatus.PENDING_CERT);
        userRepository.save(user);
        notificationService.createNotification(userId, NotificationType.SYSTEM_ANNOUNCEMENT,
                "Certification submitted", "Your certification request has been submitted.", certification.getId());
        return CertificationDto.from(certification);
    }

    @Override
    public CertificationDto getMyCertification(Long userId) {
        return certificationRepository.findByUserId(userId)
                .map(CertificationDto::from)
                .orElse(null);
    }

    @Override
    public CertificationDto review(Long id, boolean approve, String reason, Long reviewerId) {
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (reviewer.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅管理员可操作");
        }

        Certification certification = certificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CERTIFICATION_NOT_FOUND));
        if (certification.getStatus() != CertStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "该认证已处理");
        }

        User applicant = userRepository.findById(certification.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (approve) {
            certification.approve(reviewerId);
            applicant.setStatus(UserStatus.ACTIVE);
        } else {
            if (isBlank(reason)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写拒绝原因");
            }
            certification.reject(reviewerId, reason);
            applicant.setStatus(UserStatus.PENDING_CERT);
        }

        certificationRepository.save(certification);
        userRepository.save(applicant);
        notificationService.createNotification(applicant.getId(), NotificationType.SYSTEM_ANNOUNCEMENT,
                approve ? "Certification approved" : "Certification rejected",
                approve ? "Your certification has been approved." : "Your certification has been rejected.", certification.getId());
        return CertificationDto.from(certification);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
