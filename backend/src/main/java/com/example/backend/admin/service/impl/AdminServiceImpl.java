package com.example.backend.admin.service.impl;

import com.example.backend.admin.dto.AdminUserDto;
import com.example.backend.admin.dto.AdminOrderManageDto;
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
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.demand.dto.ArbitrationDto;
import com.example.backend.demand.dto.ArbitrationResolveDto;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.entity.Review;
import com.example.backend.demand.repository.ArbitrationTicketRepository;
import com.example.backend.demand.repository.DemandImageRepository;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.repository.OrderStatusLogRepository;
import com.example.backend.demand.repository.ReviewRepository;
import com.example.backend.demand.service.ArbitrationService;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.dto.CertificationDto;
import com.example.backend.user.dto.CertificationReviewDto;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CertificationService;
import com.example.backend.user.service.QuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DemandRepository demandRepository;
    private final DemandImageRepository demandImageRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ArbitrationTicketRepository arbitrationTicketRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final NotificationService notificationService;
    private final AdminOperationLogRepository adminOperationLogRepository;
    private final CertificationService certificationService;
    private final ArbitrationService arbitrationService;
    private final QuotaService quotaService;

    public AdminServiceImpl(UserRepository userRepository,
                            DemandRepository demandRepository,
                            DemandImageRepository demandImageRepository,
                            OrderRepository orderRepository,
                            ReviewRepository reviewRepository,
                            ArbitrationTicketRepository arbitrationTicketRepository,
                            OrderStatusLogRepository orderStatusLogRepository,
                            NotificationService notificationService,
                            AdminOperationLogRepository adminOperationLogRepository) {
        this(userRepository, demandRepository, demandImageRepository, orderRepository, reviewRepository, arbitrationTicketRepository,
                orderStatusLogRepository, notificationService, adminOperationLogRepository, null, null, null);
    }

    @Autowired
    public AdminServiceImpl(UserRepository userRepository,
                            DemandRepository demandRepository,
                            DemandImageRepository demandImageRepository,
                            OrderRepository orderRepository,
                            ReviewRepository reviewRepository,
                            ArbitrationTicketRepository arbitrationTicketRepository,
                            OrderStatusLogRepository orderStatusLogRepository,
                            NotificationService notificationService,
                            AdminOperationLogRepository adminOperationLogRepository,
                            CertificationService certificationService,
                            ArbitrationService arbitrationService,
                            QuotaService quotaService) {
        this.userRepository = userRepository;
        this.demandRepository = demandRepository;
        this.demandImageRepository = demandImageRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.arbitrationTicketRepository = arbitrationTicketRepository;
        this.orderStatusLogRepository = orderStatusLogRepository;
        this.notificationService = notificationService;
        this.adminOperationLogRepository = adminOperationLogRepository;
        this.certificationService = certificationService;
        this.arbitrationService = arbitrationService;
        this.quotaService = quotaService;
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
    public List<AdminOrderManageDto> listOrders(String status) {
        assertKnownManageStatus(status);
        List<Order> orders = orderRepository.findAll();
        Set<Long> orderedDemandIds = new HashSet<Long>();
        List<AdminOrderManageDto> rows = new ArrayList<AdminOrderManageDto>();
        for (Order order : orders) {
            orderedDemandIds.add(order.getDemandId());
            if (matchesStatus(status, order.getStatus())) {
                rows.add(toOrderManageDto(order));
            }
        }
        for (Demand demand : demandRepository.findAll()) {
            if (orderedDemandIds.contains(demand.getId()) || !matchesStatus(status, demand.getStatus())) {
                continue;
            }
            rows.add(toDemandManageDto(demand));
        }
        rows.sort(Comparator.comparing(AdminOrderManageDto::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return rows;
    }

    @Override
    public void banUser(Long userId, Long adminId, BanUserDto req) {
        if (adminId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "管理员不能封禁自己");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能封禁其他管理员");
        }
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

    @Override
    @Transactional
    public void deleteOrder(Long orderId, Long adminId) {
        if (orderId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单ID不能为空");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        boolean active = order.getStatus() == OrderStatus.ACCEPTED || order.getStatus() == OrderStatus.PENDING_CONFIRM;
        if (active) {
            demandRepository.findById(order.getDemandId()).ifPresent(demand -> {
                demand.setStatus(DemandStatus.OPEN);
                demandRepository.save(demand);
            });
            if (quotaService != null && order.getAcceptorDepositAmount() > 0) {
                quotaService.refundOrderDeposit(order.getAcceptorId(), order.getId(),
                        order.getAcceptorDepositAmount(), "管理员删除订单，退回接单保证金");
            }
        }
        if (orderStatusLogRepository != null) {
            orderStatusLogRepository.deleteByOrderId(orderId);
        }
        orderRepository.deleteById(orderId);
        log(adminId, "DELETE_ORDER", "order", orderId, order.getStatus().name());
    }

    @Override
    @Transactional
    public void deleteDemand(Long demandId, Long adminId) {
        if (demandId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Demand ID cannot be empty");
        }
        Demand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEMAND_NOT_FOUND));
        if (orderRepository.findByDemandId(demandId).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT, "Demand already has an order");
        }
        demandImageRepository.deleteByDemandId(demandId);
        demandRepository.deleteById(demandId);
        if (quotaService != null && demand.getStatus() != DemandStatus.CANCELLED && demand.getStatus() != DemandStatus.COMPLETED) {
            quotaService.refundDemandReward(demand.getPublisherId(), demand.getId(),
                    demand.getRewardType(), demand.getRewardAmount(), "Admin deleted demand");
        }
        log(adminId, "DELETE_DEMAND", "demand", demandId, demand.getStatus().name());
    }

    private AdminOrderManageDto toOrderManageDto(Order order) {
        AdminOrderManageDto dto = new AdminOrderManageDto();
        dto.setRowType("ORDER");
        dto.setOrderId(order.getId());
        dto.setDemandId(order.getDemandId());
        dto.setPublisherId(order.getPublisherId());
        dto.setPublisherName(userDisplayName(order.getPublisherId()));
        dto.setAcceptorId(order.getAcceptorId());
        dto.setAcceptorName(userDisplayName(order.getAcceptorId()));
        dto.setStatus(order.getStatus() == null ? null : order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        demandRepository.findById(order.getDemandId()).ifPresent(demand -> dto.setDemandTitle(demand.getTitle()));
        return dto;
    }

    private AdminOrderManageDto toDemandManageDto(Demand demand) {
        AdminOrderManageDto dto = new AdminOrderManageDto();
        dto.setRowType("DEMAND");
        dto.setDemandId(demand.getId());
        dto.setDemandTitle(demand.getTitle());
        dto.setPublisherId(demand.getPublisherId());
        dto.setPublisherName(userDisplayName(demand.getPublisherId()));
        dto.setStatus(demand.getStatus() == null ? null : demand.getStatus().name());
        dto.setCreatedAt(demand.getCreatedAt());
        return dto;
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
        dto.setQuotaAvailable(user.getQuotaAvailable());
        dto.setQuotaFrozen(user.getQuotaFrozen());
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

    private void assertKnownManageStatus(String status) {
        if (isBlank(status)) {
            return;
        }
        try {
            OrderStatus.valueOf(status.toUpperCase(Locale.ROOT));
            return;
        } catch (IllegalArgumentException ex) {
        }
        try {
            DemandStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "Status is invalid");
        }
    }

    private boolean matchesStatus(String filter, Enum<?> status) {
        return isBlank(filter) || (status != null && status.name().equalsIgnoreCase(filter));
    }

    private String userDisplayName(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("user" + userId);
    }
}
