package com.example.backend.user.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.CertStatus;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.dto.BanAppealDto;
import com.example.backend.user.entity.BanAppeal;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.BanAppealRepository;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.BanAppealService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BanAppealServiceImpl implements BanAppealService {

    private final BanAppealRepository banAppealRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public BanAppealServiceImpl(BanAppealRepository banAppealRepository,
                                UserRepository userRepository,
                                NotificationService notificationService) {
        this.banAppealRepository = banAppealRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public BanAppealDto submitAppeal(Long userId, String reason) {
        User user = requireUser(userId);
        if (user.getStatus() != UserStatus.BANNED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only banned users can submit ban appeals");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Appeal reason cannot be empty");
        }
        if (banAppealRepository.findPendingByUserId(userId).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT, "There is already a pending ban appeal");
        }

        BanAppeal appeal = new BanAppeal();
        appeal.setUserId(userId);
        appeal.setReason(reason.trim());
        appeal.setStatus(CertStatus.PENDING);
        banAppealRepository.save(appeal);
        return BanAppealDto.from(appeal);
    }

    @Override
    public BanAppealDto getMyLatestAppeal(Long userId) {
        return BanAppealDto.from(banAppealRepository.findLatestByUserId(userId).orElse(null));
    }

    @Override
    public List<BanAppealDto> listPendingAppeals() {
        return banAppealRepository.findByStatusOrderByCreatedAtAsc(CertStatus.PENDING)
                .stream()
                .map(BanAppealDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public BanAppealDto reviewAppeal(Long appealId, Long adminId, boolean approved) {
        User admin = requireUser(adminId);
        if (admin.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        BanAppeal appeal = banAppealRepository.findById(appealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (appeal.getStatus() != CertStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "Appeal already processed");
        }

        appeal.setStatus(approved ? CertStatus.APPROVED : CertStatus.REJECTED);
        appeal.setReviewedBy(adminId);
        appeal.setReviewedAt(LocalDateTime.now());
        banAppealRepository.save(appeal);

        User user = requireUser(appeal.getUserId());
        if (approved) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            notificationService.createNotification(
                    user.getId(),
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    "Ban appeal approved",
                    "Your ban appeal has been approved and your account is active again.",
                    null
            );
        } else {
            notificationService.createNotification(
                    user.getId(),
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    "Ban appeal rejected",
                    "Your ban appeal has been rejected.",
                    null
            );
        }

        return BanAppealDto.from(appeal);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
