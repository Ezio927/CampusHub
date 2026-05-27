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
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.entity.Review;
import com.example.backend.demand.repository.ArbitrationTicketRepository;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.repository.ReviewRepository;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
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

    public AdminServiceImpl(UserRepository userRepository,
                            DemandRepository demandRepository,
                            OrderRepository orderRepository,
                            ReviewRepository reviewRepository,
                            ArbitrationTicketRepository arbitrationTicketRepository,
                            NotificationService notificationService,
                            AdminOperationLogRepository adminOperationLogRepository) {
        this.userRepository = userRepository;
        this.demandRepository = demandRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.arbitrationTicketRepository = arbitrationTicketRepository;
        this.notificationService = notificationService;
        this.adminOperationLogRepository = adminOperationLogRepository;
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

    private void log(Long adminId, String action, String targetType, Long targetId, String remark) {
        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(adminId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setRemark(remark);
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
