package com.example.backend.user.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.CertStatus;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.common.validation.ImageDataUrlValidator;
import com.example.backend.user.dto.CertificationDto;
import com.example.backend.user.dto.CertificationSubmitDto;
import com.example.backend.user.entity.Certification;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.CertificationRepository;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CertificationService;
import com.example.backend.user.service.QuotaService;
import com.example.backend.user.validation.UserProfileValidator;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.common.enums.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CertificationServiceImpl implements CertificationService {

    private final CertificationRepository certificationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final QuotaService quotaService;

    public CertificationServiceImpl(CertificationRepository certificationRepository,
                                    UserRepository userRepository,
                                    NotificationService notificationService,
                                    QuotaService quotaService) {
        this.certificationRepository = certificationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.quotaService = quotaService;
    }

    @Override
    @Transactional
    public CertificationDto submit(Long userId, CertificationSubmitDto req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException(ErrorCode.ACCOUNT_BANNED);
        }
        if (req == null || req.getCertType() == null || isBlank(req.getStudentId()) || req.getGender() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "认证类型、学号和性别不能为空");
        }
        String studentId = UserProfileValidator.requireValidStudentId(req.getStudentId());
        String cardImageUrl = ImageDataUrlValidator.requireValidImageDataUrl(req.getCardImageUrl(), "认证图片");

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
        certification.setStudentId(studentId);
        certification.setRealName(req.getRealName());
        certification.setGender(req.getGender());
        certification.setCardImageUrl(cardImageUrl);
        certification.setStatus(CertStatus.PENDING);
        certification.setRejectReason(null);
        certification.setReviewedBy(null);
        certification.setReviewedAt(null);
        certificationRepository.save(certification);

        user.setStatus(UserStatus.PENDING_CERT);
        userRepository.save(user);
        notificationService.createNotification(userId, NotificationType.SYSTEM_ANNOUNCEMENT,
                "实名认证已提交", "你的实名认证申请已提交，请等待审核。", certification.getId());
        return CertificationDto.from(certification);
    }

    @Override
    public CertificationDto getMyCertification(Long userId) {
        return certificationRepository.findByUserId(userId)
                .map(CertificationDto::from)
                .orElse(null);
    }

    @Override
    @Transactional
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
            applicant.setStudentId(certification.getStudentId());
            applicant.setRealName(certification.getRealName());
            applicant.setGender(certification.getGender());
            quotaService.grantCertificationReward(applicant.getId(), reviewerId);
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
                approve ? "实名认证已通过" : "实名认证已驳回",
                approve ? "你的实名认证已通过。" : "你的实名认证已被驳回。", certification.getId());
        return CertificationDto.from(certification);
    }

    @Override
    public List<CertificationDto> listPending() {
        return certificationRepository.findByStatus(CertStatus.PENDING).stream()
                .map(CertificationDto::from)
                .collect(java.util.stream.Collectors.toList());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
