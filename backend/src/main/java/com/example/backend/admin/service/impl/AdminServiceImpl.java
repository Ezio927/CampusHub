package com.example.backend.admin.service.impl;

import com.example.backend.admin.dto.AdminUserDto;
import com.example.backend.admin.dto.BanUserDto;
import com.example.backend.admin.dto.DashboardDto;
import com.example.backend.admin.entity.AdminOperationLog;
import com.example.backend.admin.repository.AdminOperationLogRepository;
import com.example.backend.admin.service.AdminService;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.demand.dto.ArbitrationDto;
import com.example.backend.demand.dto.ArbitrationResolveDto;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.entity.Review;
import com.example.backend.demand.repository.ArbitrationTicketRepository;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.repository.ReviewRepository;
import com.example.backend.demand.service.ArbitrationService;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.dto.CertificationDto;
import com.example.backend.user.dto.CertificationReviewDto;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CertificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DemandRepository demandRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ArbitrationTicketRepository arbitrationTicketRepository;
    private final NotificationService notificationService;
    private final AdminOperationLogRepository adminOperationLogRepository;
    private final CertificationService certificationService;
    private final ArbitrationService arbitrationService;

    public AdminServiceImpl(UserRepository userRepository,
                            DemandRepository demandRepository,
                            OrderRepository orderRepository,
                            ReviewRepository reviewRepository,
                            ArbitrationTicketRepository arbitrationTicketRepository,
                            NotificationService notificationService,
                            AdminOperationLogRepository adminOperationLogRepository) {
        this(userRepository, demandRepository, orderRepository, reviewRepository, arbitrationTicketRepository,
                notificationService, adminOperationLogRepository, null, null);
    }

    @Autowired
    public AdminServiceImpl(UserRepository userRepository,
                            DemandRepository demandRepository,
                            OrderRepository orderRepository,
                            ReviewRepository reviewRepository,
                            ArbitrationTicketRepository arbitrationTicketRepository,
                            NotificationService notificationService,
                            AdminOperationLogRepository adminOperationLogRepository,
                            CertificationService certificationService,
                            ArbitrationService arbitrationService) {
        this.userRepository = userRepository;
        this.demandRepository = demandRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.arbitrationTicketRepository = arbitrationTicketRepository;
        this.notificationService = notificationService;
        this.adminOperationLogRepository = adminOperationLogRepository;
        this.certificationService = certificationService;
        this.arbitrationService = arbitrationService;
    }

    @Override
    public DashboardDto getDashboard() {
        DashboardDto dto = new DashboardDto();
        dto.setTotalUsers(userRepository.findAll().size());
        dto.setActiveDemands(demandRepository.findAll().stream().filter(demand -> demand.getStatus() == DemandStatus.OPEN).count());
        dto.setCompletedOrders(orderRepository.findAll().stream().filter(order -> order.getStatus() == OrderStatus.COMPLETED).count());
        List<Review> reviews = reviewRepository.findAll();
        double avg = reviews.isEmpty() ? 0.0 : reviews.stream().mapToInt(review -> review.getRating() == null ? 0 : review.getRating()).average().orElse(0.0);
        dto.setAverageRating(avg);
        dto.setPendingArbitrations(arbitrationTicketRepository.findByStatusOrderByCreatedAtAsc(com.example.backend.common.enums.ArbitrationStatus.PENDING).size());
        return dto;
    }

    @Override
    public List<AdminUserDto> listUsers(String keyword, String status) {
        return userRepository.findAll().stream()
                .filter(user -> isBlank(keyword) || containsIgnoreCase(user.getUsername(), keyword)
                        || containsIgnoreCase(user.getRealName(), keyword)
                        || containsIgnoreCase(user.getStudentId(), keyword))
                .filter(user -> isBlank(status) || user.getStatus().name().equalsIgnoreCase(status))
                .map(this::toAdminUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void banUser(Long userId, Long adminId, BanUserDto req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
        notificationService.createNotification(userId, NotificationType.SYSTEM_ANNOUNCEMENT,
                "Account banned", req == null ? "Your account has been banned." : req.getReason(), null);
        log(adminId, "BAN_USER", "user", userId, req == null ? null : req.getReason());
    }

    @Override
    public void unbanUser(Long userId, Long adminId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        notificationService.createNotification(userId, NotificationType.SYSTEM_ANNOUNCEMENT,
                "Account unbanned", "Your account has been restored.", null);
        log(adminId, "UNBAN_USER", "user", userId, null);
    }

    @Override
    public CertificationDto reviewCertification(Long certificationId, Long adminId, CertificationReviewDto req) {
        if (certificationService == null) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "认证服务不可用");
        }
        if (req == null || req.getApprove() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择通过或拒绝");
        }
        CertificationDto reviewed = certificationService.review(
                certificationId, req.getApprove(), req.getReason(), adminId);
        log(adminId, "REVIEW_CERTIFICATION", "certification", certificationId,
                req.getApprove() ? "approved" : "rejected: " + req.getReason());
        return reviewed;
    }

    @Override
    public ArbitrationDto resolveArbitration(Long ticketId, Long adminId, ArbitrationResolveDto req) {
        if (arbitrationService == null) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "仲裁服务不可用");
        }
        ArbitrationDto resolved = arbitrationService.resolve(ticketId, adminId, req);
        String detail = req == null ? null : req.getAction() + ": " + req.getResolution();
        log(adminId, "RESOLVE_ARBITRATION", "arbitration", ticketId, detail);
        return resolved;
    }

    private AdminUserDto toAdminUserDto(User user) {
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setStudentId(user.getStudentId());
        dto.setRealName(user.getRealName());
        dto.setStatus(user.getStatus().name());
        dto.setRole(user.getRole().name());
        dto.setCreditScore(user.getCreditScore());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    // [Human-written] Centralizes the audit trail so every privileged action is recorded consistently.
    private void log(Long adminId, String operation, String targetType, Long targetId, String detail) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(adminId);
        log.setOperation(operation);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        adminOperationLogRepository.save(log);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (source == null || keyword == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }
}
