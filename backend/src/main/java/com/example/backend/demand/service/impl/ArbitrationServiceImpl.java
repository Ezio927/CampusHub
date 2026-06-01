package com.example.backend.demand.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.ArbitrationStatus;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.common.enums.UserRole;
import com.example.backend.demand.dto.ArbitrationCreateDto;
import com.example.backend.demand.dto.ArbitrationDto;
import com.example.backend.demand.dto.ArbitrationResolveDto;
import com.example.backend.demand.entity.ArbitrationTicket;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.repository.ArbitrationTicketRepository;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.service.ArbitrationService;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CreditService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArbitrationServiceImpl implements ArbitrationService {

    private final ArbitrationTicketRepository arbitrationTicketRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CreditService creditService;
    private final OrderRepository orderRepository;

    public ArbitrationServiceImpl(ArbitrationTicketRepository arbitrationTicketRepository,
                                  UserRepository userRepository,
                                  NotificationService notificationService,
                                  CreditService creditService,
                                  OrderRepository orderRepository) {
        this.arbitrationTicketRepository = arbitrationTicketRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.creditService = creditService;
        this.orderRepository = orderRepository;
    }

    @Override
    public ArbitrationDto createTicket(Long reporterId, Long orderId, Long reviewId, String reason, List<String> evidenceUrls) {
        ArbitrationTicket ticket = new ArbitrationTicket();
        ticket.setReporterId(reporterId);
        ticket.setOrderId(orderId);
        ticket.setReviewId(reviewId);
        ticket.setReason(reason);
        ticket.setEvidenceUrls(evidenceUrls);
        ticket.setStatus(ArbitrationStatus.PENDING);
        arbitrationTicketRepository.save(ticket);
        notificationService.createNotification(reporterId, NotificationType.ARBITRATION_UPDATE,
                "仲裁工单已创建", "您的仲裁申请已提交，管理员会尽快处理。", ticket.getId());
        return ArbitrationDto.from(ticket);
    }

    @Override
    public ArbitrationDto createTicket(Long reporterId, ArbitrationCreateDto req) {
        if (req == null || req.getOrderId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单ID不能为空");
        }
        return createTicket(reporterId, req.getOrderId(), req.getReviewId(), req.getReason(), req.getEvidenceUrls());
    }

    @Override
    public List<ArbitrationDto> listPending() {
        return arbitrationTicketRepository.findByStatusOrderByCreatedAtAsc(ArbitrationStatus.PENDING)
                .stream()
                .map(ArbitrationDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public ArbitrationDto resolve(Long ticketId, Long adminId, ArbitrationResolveDto req) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (admin.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        ArbitrationTicket ticket = arbitrationTicketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (ticket.getStatus() != ArbitrationStatus.PENDING && ticket.getStatus() != ArbitrationStatus.PROCESSING) {
            throw new BusinessException(ErrorCode.CONFLICT, "仲裁工单已处理");
        }
        String action = req == null ? null : req.getAction();
        if ("DISMISS".equalsIgnoreCase(action)) {
            ticket.setStatus(ArbitrationStatus.DISMISSED);
        } else {
            ticket.setStatus(ArbitrationStatus.RESOLVED);
        }
        ticket.setResolution(req == null ? null : req.getResolution());
        ticket.setResolvedBy(adminId);
        ticket.setResolvedAt(LocalDateTime.now());
        arbitrationTicketRepository.save(ticket);

        if ("RESOLVED".equalsIgnoreCase(action) && ticket.getOrderId() != null) {
            orderRepository.findById(ticket.getOrderId()).ifPresent(order -> {
                creditService.deductCredit(order.getAcceptorId(), 10,
                        "仲裁判定违约", order.getId());
            });
        } else if ("DISMISS".equalsIgnoreCase(action)) {
            creditService.deductCredit(ticket.getReporterId(), 10,
                    "恶意差评", ticket.getOrderId());
        }

        notificationService.createNotification(ticket.getReporterId(), NotificationType.ARBITRATION_UPDATE,
                "仲裁已处理", req == null ? "" : req.getResolution(), ticket.getId());
        return ArbitrationDto.from(ticket);
    }
}
