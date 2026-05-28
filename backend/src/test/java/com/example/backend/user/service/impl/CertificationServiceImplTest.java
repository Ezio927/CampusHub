package com.example.backend.user.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.*;
import com.example.backend.notification.entity.Notification;
import com.example.backend.notification.repository.NotificationRepository;
import com.example.backend.notification.service.impl.NotificationServiceImpl;
import com.example.backend.user.dto.CertificationDto;
import com.example.backend.user.dto.CertificationSubmitDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.CertificationRepository;
import com.example.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CertificationServiceImplTest {

    private UserRepository userRepository;
    private CertificationRepository certificationRepository;
    private CertificationServiceImpl certificationService;
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        certificationRepository = new CertificationRepository();
        notificationService = new NotificationServiceImpl(new NotificationRepository());
        certificationService = new CertificationServiceImpl(
                certificationRepository, userRepository, notificationService);

        User alice = userRepository.findById(1L).orElseThrow();
        alice.setStatus(UserStatus.ACTIVE);
        userRepository.save(alice);
    }

    @Test
    void shouldRejectSubmitWithNullRequest() {
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> certificationService.submit(1L, null)).getCode());
    }

    @Test
    void shouldRejectSubmitWithMissingCertType() {
        CertificationSubmitDto dto = new CertificationSubmitDto();
        dto.setStudentId("2024001");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> certificationService.submit(1L, dto)).getCode());
    }

    @Test
    void shouldRejectSubmitWithMissingStudentId() {
        CertificationSubmitDto dto = new CertificationSubmitDto();
        dto.setCertType(CertType.STUDENT_ID);
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> certificationService.submit(1L, dto)).getCode());
    }

    @Test
    void shouldRequireCardImageForStudentCard() {
        CertificationSubmitDto dto = new CertificationSubmitDto();
        dto.setCertType(CertType.STUDENT_CARD);
        dto.setStudentId("2024001");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> certificationService.submit(1L, dto)).getCode());
    }

    @Test
    void shouldRejectSubmitWhenBanned() {
        User user = new User();
        user.setUsername("banned-user");
        user.setPasswordHash("hash");
        user.setStudentId("BAN001");
        user.setRealName("Banned");
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);

        CertificationSubmitDto dto = buildSubmitDto(CertType.STUDENT_ID, "BAN001", null);
        Assertions.assertEquals(ErrorCode.ACCOUNT_BANNED.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> certificationService.submit(user.getId(), dto)).getCode());
    }

    @Test
    void shouldSubmitAndReviewCertification() {
        CertificationSubmitDto dto = buildSubmitDto(CertType.STUDENT_ID, "2024001001", null);
        CertificationDto result = certificationService.submit(1L, dto);

        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals(CertStatus.PENDING, result.getStatus());

        CertificationDto myCert = certificationService.getMyCertification(1L);
        Assertions.assertNotNull(myCert);

        Assertions.assertEquals(ErrorCode.CERTIFICATION_PENDING_EXISTS.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> certificationService.submit(1L, dto)).getCode());
    }

    @Test
    void shouldRejectReviewByNonAdmin() {
        CertificationSubmitDto dto = buildSubmitDto(CertType.STUDENT_ID, "2024001001", null);
        CertificationDto cert = certificationService.submit(1L, dto);

        Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> certificationService.review(cert.getId(), true, null, 1L)).getCode());
    }

    @Test
    void shouldApproveCertificationAndActivateUser() {
        CertificationSubmitDto dto = buildSubmitDto(CertType.STUDENT_ID, "2024001001", null);
        CertificationDto cert = certificationService.submit(1L, dto);

        certificationService.review(cert.getId(), true, null, 2L);
        User alice = userRepository.findById(1L).orElseThrow();
        Assertions.assertEquals(UserStatus.ACTIVE, alice.getStatus());
    }

    @Test
    void shouldRejectCertificationWithReason() {
        CertificationSubmitDto dto = buildSubmitDto(CertType.STUDENT_ID, "2024001001", null);
        CertificationDto cert = certificationService.submit(1L, dto);

        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> certificationService.review(cert.getId(), false, null, 2L)).getCode());

        certificationService.review(cert.getId(), false, "invalid photo", 2L);
    }

    @Test
    void shouldAllowResubmitAfterRejection() {
        CertificationSubmitDto dto = buildSubmitDto(CertType.STUDENT_ID, "2024001001", null);
        certificationService.submit(1L, dto);
        certificationService.review(
                certificationRepository.findByUserId(1L).orElseThrow().getId(),
                false, "bad", 2L);

        CertificationSubmitDto dto2 = buildSubmitDto(CertType.STUDENT_ID, "2024001001", null);
        CertificationDto result = certificationService.submit(1L, dto2);
        Assertions.assertNotNull(result.getId());
    }

    private CertificationSubmitDto buildSubmitDto(CertType certType, String studentId, String imageUrl) {
        CertificationSubmitDto dto = new CertificationSubmitDto();
        dto.setCertType(certType);
        dto.setStudentId(studentId);
        dto.setCardImageUrl(imageUrl);
        return dto;
    }
}
