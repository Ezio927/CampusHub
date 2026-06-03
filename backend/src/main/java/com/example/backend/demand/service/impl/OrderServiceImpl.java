package com.example.backend.demand.service.impl;

import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.dto.ConversationDto;
import com.example.backend.chat.service.ConversationService;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.ratelimit.ActionRateLimitService;
import com.example.backend.common.ratelimit.RateLimitAction;
import com.example.backend.common.ratelimit.RateLimitProperties;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.demand.dto.AcceptOrderDto;
import com.example.backend.demand.dto.DemandDto;
import com.example.backend.demand.dto.OrderCancelDto;
import com.example.backend.demand.dto.OrderDto;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.entity.OrderStatusLog;
import com.example.backend.demand.pattern.observer.OrderEventPublisher;
import com.example.backend.demand.pattern.strategy.OrderAction;
import com.example.backend.demand.pattern.strategy.OrderStateContext;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.repository.OrderStatusLogRepository;
import com.example.backend.demand.repository.ReviewRepository;
import com.example.backend.demand.service.DemandService;
import com.example.backend.demand.service.OrderService;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.dto.QuotaRecordDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CreditService;
import com.example.backend.user.service.QuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final int FREE_CANCEL_MINUTES = 10;
    private static final int LATE_CANCEL_PENALTY = 10;

    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final OrderStateContext orderStateContext;
    private final OrderEventPublisher orderEventPublisher;
    private final DemandService demandService;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CreditService creditService;
    private final QuotaService quotaService;
    private final ConversationService conversationService;
    private final NotificationService notificationService;
    private final ActionRateLimitService actionRateLimitService;

    @Autowired
    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderStatusLogRepository orderStatusLogRepository,
            OrderStateContext orderStateContext,
            OrderEventPublisher orderEventPublisher,
            DemandService demandService,
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            CreditService creditService,
            QuotaService quotaService,
            ConversationService conversationService,
            NotificationService notificationService,
            ActionRateLimitService actionRateLimitService) {
        this.orderRepository = orderRepository;
        this.orderStatusLogRepository = orderStatusLogRepository;
        this.orderStateContext = orderStateContext;
        this.orderEventPublisher = orderEventPublisher;
        this.demandService = demandService;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.creditService = creditService;
        this.quotaService = quotaService;
        this.conversationService = conversationService;
        this.notificationService = notificationService;
        this.actionRateLimitService = actionRateLimitService;
    }

    // [Human-written] 兼容旧测试构造签名：把 DemandRepository 适配成最小 DemandService 能力。
    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderStatusLogRepository orderStatusLogRepository,
            DemandRepository demandRepository,
            UserRepository userRepository,
            CreditService creditService,
            QuotaService quotaService,
            ConversationService conversationService,
            NotificationService notificationService) {
        this(
                orderRepository,
                orderStatusLogRepository,
                new OrderStateContext(),
                new OrderEventPublisher(event -> {
                }),
                new DemandRepositoryDemandServiceAdapter(demandRepository),
                null,
                userRepository,
                creditService,
                quotaService,
                conversationService,
                notificationService,
                new ActionRateLimitService(new RateLimitProperties()));
    }

    @Override
    @Transactional
    public OrderDto acceptDemand(Long userId, AcceptOrderDto req) {
        actionRateLimitService.checkAndRecord(userId, RateLimitAction.ORDER_ACCEPT);
        User acceptor = requireActiveUser(userId);
        if (req == null || req.getDemandId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "需求ID不能为空");
        }
        if (!creditService.checkCredit(userId)) {
            throw new BusinessException(ErrorCode.CREDIT_INSUFFICIENT);
        }
        int acceptorCredit = creditService.getCreditScore(userId);
        int depositAmount = acceptorCredit < 70 ? QuotaService.LOW_CREDIT_DEPOSIT : 0;

        DemandDto demand = demandService.getDetail(req.getDemandId());
        if (demand == null) {
            throw new BusinessException(ErrorCode.DEMAND_NOT_FOUND);
        }
        if (demand.getPublisherId() == null || demand.getPublisherId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能接自己发布的需求");
        }
        if (demand.getStatus() != DemandStatus.OPEN) {
            throw new BusinessException(ErrorCode.DEMAND_ALREADY_ACCEPTED);
        }
        if (orderRepository.findActiveByDemandId(demand.getId()).isPresent()) {
            throw new BusinessException(ErrorCode.DEMAND_ALREADY_ACCEPTED);
        }

        demandService.updateStatus(demand.getId(), DemandStatus.ACCEPTED);

        Order order = new Order();
        order.setDemandId(demand.getId());
        order.setPublisherId(demand.getPublisherId());
        order.setAcceptorId(acceptor.getId());
        order.setStatus(OrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());
        order.setAutoConfirmDeadline(LocalDateTime.now().plusHours(24));
        order.setAcceptorDepositAmount(depositAmount);
        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.DEMAND_ALREADY_ACCEPTED);
        }
        if (depositAmount > 0) {
            quotaService.freezeOrderDeposit(userId, order.getId(), depositAmount);
        }

        appendLogAndPublish(order, null, userId, "accept demand");
        postOrderSystemMessage(order, "平台消息：" + userDisplayName(acceptor.getId())
                + " 已接取需求「" + demandTitle(demand.getId()) + "」。", userId);
        notificationService.createNotification(demand.getPublisherId(), NotificationType.ORDER_ACCEPTED,
                "需求已被接取", userDisplayName(acceptor.getId()) + " 已接取你的需求「" + demandTitle(demand.getId()) + "」", order.getId());

        return enrichDto(order);
    }

    @Override
    public List<OrderDto> list(Long userId, String role, String status) {
        OrderStatus orderStatus = parseStatus(status);
        List<Order> orders;
        if (role == null || role.isBlank()) {
            orders = new ArrayList<Order>();
            orders.addAll(orderRepository.findByPublisherIdAndStatus(userId, orderStatus));
            orders.addAll(orderRepository.findByAcceptorIdAndStatus(userId, orderStatus));
            Map<Long, Order> deduplicated = new LinkedHashMap<Long, Order>();
            for (Order order : orders) {
                deduplicated.put(order.getId(), order);
            }
            orders = new ArrayList<Order>(deduplicated.values());
            orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        } else if ("ACCEPTOR".equalsIgnoreCase(role)) {
            orders = orderRepository.findByAcceptorIdAndStatus(userId, orderStatus);
        } else if ("PUBLISHER".equalsIgnoreCase(role)) {
            orders = orderRepository.findByPublisherIdAndStatus(userId, orderStatus);
        } else {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "角色参数无效");
        }
        return orders.stream().map(this::enrichDto).collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> history(Long userId) {
        return orderRepository.findHistoryByUserId(userId)
                .stream()
                .map(this::enrichDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto getDetail(Long orderId, Long userId) {
        return enrichDto(requireParticipantOrder(orderId, userId));
    }

    @Override
    @Transactional
    public OrderDto deliver(Long orderId, Long userId) {
        Order order = requireParticipantOrder(orderId, userId);
        if (!order.isAcceptor(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有接单者才能标记送达");
        }
        orderStateContext.assertCanOperate(order, OrderAction.MARK_DELIVERED);

        OrderStatus fromStatus = order.getStatus();
        order.setStatus(OrderStatus.PENDING_CONFIRM);
        order.setAutoConfirmDeadline(LocalDateTime.now().plusHours(24));
        orderRepository.save(order);

        appendLogAndPublish(order, fromStatus, userId, "deliver");
        postOrderSystemMessage(order, "平台消息：" + userDisplayName(order.getAcceptorId())
                + " 已标记需求「" + demandTitle(order.getDemandId()) + "」已完成，等待发布者确认。", userId);
        notificationService.createNotification(order.getPublisherId(), NotificationType.ORDER_COMPLETED,
                "需求已完成，请确认", userDisplayName(order.getAcceptorId()) + " 已标记需求「" + demandTitle(order.getDemandId()) + "」已完成", order.getId());
        return enrichDto(order);
    }

    @Override
    @Transactional
    public OrderDto confirm(Long orderId, Long userId) {
        Order order = requireParticipantOrder(orderId, userId);
        orderStateContext.assertCanOperate(order, OrderAction.CONFIRM_COMPLETED);
        if (!order.isPublisher(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有发布者才能确认完成");
        }

        OrderStatus fromStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);
        settleCompletedOrder(order);

        appendLogAndPublish(order, fromStatus, userId, "confirm");
        postOrderSystemMessage(order, "平台消息：发布者已确认需求「"
                + demandTitle(order.getDemandId()) + "」完成。", userId);
        notificationService.createNotification(order.getAcceptorId(), NotificationType.ORDER_COMPLETED,
                "订单已完成", "发布者已确认需求「" + demandTitle(order.getDemandId()) + "」完成", order.getId());
        return enrichDto(order);
    }

    @Override
    @Transactional
    public OrderDto cancel(Long orderId, Long userId, OrderCancelDto req) {
        Order order = requireParticipantOrder(orderId, userId);
        orderStateContext.assertCanOperate(order, OrderAction.CANCEL);
        String reason = req == null || req.getReason() == null ? null : req.getReason().trim();
        if (reason == null || reason.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "取消原因不能为空");
        }

        OrderStatus fromStatus = order.getStatus();
        int penalty = calculateCancelPenalty(order, LocalDateTime.now());
        if (penalty > 0) {
            creditService.deductCredit(userId, penalty, "取消订单", order.getId());
        }
        int quotaPenalty = applyCancelQuotaEffects(order, fromStatus, userId, penalty);

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        orderRepository.save(order);

        appendLogAndPublish(order, fromStatus, userId, reason);
        postOrderSystemMessage(order, cancelSystemMessage(order, fromStatus, userId), userId);
        Long cancelNotifyUserId = order.isPublisher(userId) ? order.getAcceptorId() : order.getPublisherId();
        notificationService.createNotification(cancelNotifyUserId, NotificationType.ORDER_CANCELLED,
                "订单已取消", userDisplayName(userId) + " 已取消订单「" + demandTitle(order.getDemandId()) + "」" + (isBlank(reason) ? "" : "，原因：" + reason), order.getId());

        OrderDto dto = enrichDto(order);
        dto.setCreditPenalty(-penalty);
        dto.setQuotaPenalty(-quotaPenalty);
        return dto;
    }

    @Scheduled(fixedDelay = 600_000)
    @Transactional
    public void autoConfirmExpired() {
        LocalDateTime now = LocalDateTime.now();
        for (Order order : orderRepository.findByStatusAndAutoConfirmDeadlineBefore(OrderStatus.PENDING_CONFIRM, now)) {
            if (!order.isAutoConfirmExpired(now)) {
                continue;
            }
            OrderStatus fromStatus = order.getStatus();
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(now);
            orderRepository.save(order);
            settleCompletedOrder(order);
            appendLogAndPublish(order, fromStatus, null, "auto confirm completed");
            String autoMsg = "需求「" + demandTitle(order.getDemandId()) + "」已超过24小时自动确认完成";
            notificationService.createNotification(order.getPublisherId(), NotificationType.ORDER_COMPLETED,
                    "订单已自动确认", autoMsg, order.getId());
            notificationService.createNotification(order.getAcceptorId(), NotificationType.ORDER_COMPLETED,
                    "订单已自动确认", autoMsg, order.getId());
        }
    }

    // [Human-written] 接单后 10 分钟内取消不扣分，超过 10 分钟扣 10 分。
    int calculateCancelPenalty(Order order, LocalDateTime now) {
        if (order.getAcceptedAt() == null) {
            return LATE_CANCEL_PENALTY;
        }
        LocalDateTime freeCancelDeadline = order.getAcceptedAt().plusMinutes(FREE_CANCEL_MINUTES);
        return now.isAfter(freeCancelDeadline) ? LATE_CANCEL_PENALTY : 0;
    }

    private void appendLogAndPublish(Order order, OrderStatus fromStatus, Long operatorId, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(order.getId());
        log.setFromStatus(fromStatus);
        log.setToStatus(order.getStatus());
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        orderStatusLogRepository.save(log);
        orderEventPublisher.publish(order, fromStatus, operatorId, remark);
    }

    private void settleCompletedOrder(Order order) {
        if (order == null || quotaService == null) {
            return;
        }
        DemandDto demand = demandService.getDetail(order.getDemandId());
        if (demand != null) {
            quotaService.payDemandReward(order.getPublisherId(), order.getAcceptorId(),
                    order.getDemandId(), order.getId(), demand.getRewardType(), demand.getRewardAmount());
        }
        quotaService.refundOrderDeposit(order.getAcceptorId(), order.getId(),
                order.getAcceptorDepositAmount(), "订单完成，退回接单保证金");
        if (order.getAcceptorId() != null) {
            quotaService.rewardDailyOrderCompletion(order.getAcceptorId(), order.getId());
        }
    }

    private int applyCancelQuotaEffects(Order order, OrderStatus fromStatus, Long operatorId, int creditPenalty) {
        if (order == null || quotaService == null) {
            return 0;
        }
        int quotaPenalty = 0;
        boolean depositPenalized = false;
        if (creditPenalty > 0) {
            if (order.isAcceptor(operatorId) && order.getAcceptorDepositAmount() > 0) {
                quotaPenalty += absChange(quotaService.deductOrderDeposit(order.getAcceptorId(), order.getId(),
                        order.getAcceptorDepositAmount(), "逾期取消订单，扣除接单保证金"));
                depositPenalized = true;
            } else {
                quotaPenalty += absChange(quotaService.deductLateCancelPenalty(operatorId, order.getId(),
                        QuotaService.LATE_CANCEL_PENALTY, "逾期取消订单扣除额度分"));
            }
        }
        if (!depositPenalized) {
            quotaService.refundOrderDeposit(order.getAcceptorId(), order.getId(),
                    order.getAcceptorDepositAmount(), "订单取消，退回接单保证金");
        }
        if (!isAcceptorCancelBeforeDelivery(order, fromStatus, operatorId)) {
            refundOrderDemandReward(order);
        }
        return quotaPenalty;
    }

    private void refundOrderDemandReward(Order order) {
        DemandDto demand = demandService.getDetail(order.getDemandId());
        if (demand == null) {
            return;
        }
        quotaService.refundDemandReward(order.getPublisherId(), order.getDemandId(),
                demand.getRewardType(), demand.getRewardAmount(), "订单取消，退回冻结报酬");
    }

    private int absChange(QuotaRecordDto record) {
        if (record == null || record.getChangeAmount() == null) {
            return 0;
        }
        return Math.abs(record.getChangeAmount());
    }

    private void postOrderSystemMessage(Order order, String content, Long actorId) {
        if (order == null || order.getPublisherId() == null || order.getAcceptorId() == null || content == null) {
            return;
        }
        CreateConversationDto conversationReq = new CreateConversationDto();
        conversationReq.setTargetUserId(order.getPublisherId());
        conversationReq.setDemandId(order.getDemandId());
        ConversationDto conversation = conversationService.getOrCreate(order.getAcceptorId(), conversationReq);
        conversationService.appendSystemMessage(conversation.getId(), content, actorId);
    }

    private String cancelSystemMessage(Order order, OrderStatus fromStatus, Long operatorId) {
        String title = demandTitle(order.getDemandId());
        String reason = isBlank(order.getCancelReason()) ? "" : " 原因：" + order.getCancelReason().trim();
        if (isAcceptorCancelBeforeDelivery(order, fromStatus, operatorId)) {
            return "平台消息：" + userDisplayName(order.getAcceptorId())
                    + " 已取消接单，需求「" + title + "」已重新开放。" + reason;
        }
        if (order.isPublisher(operatorId)) {
            return "平台消息：发布者已取消需求「" + title + "」。" + reason;
        }
        return "平台消息：" + userDisplayName(operatorId) + " 已取消订单「" + title + "」。" + reason;
    }

    private boolean isAcceptorCancelBeforeDelivery(Order order, OrderStatus fromStatus, Long operatorId) {
        return order != null
                && order.isAcceptor(operatorId)
                && fromStatus == OrderStatus.ACCEPTED;
    }

    private String userDisplayName(Long userId) {
        if (userId == null) {
            return "用户";
        }
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("用户" + userId);
    }

    private String demandTitle(Long demandId) {
        if (demandId == null) {
            return "需求";
        }
        try {
            DemandDto demand = demandService.getDetail(demandId);
            if (demand != null && !isBlank(demand.getTitle())) {
                return demand.getTitle();
            }
        } catch (RuntimeException ignored) {
        }
        return "需求 #" + demandId;
    }

    private User requireActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException(ErrorCode.ACCOUNT_BANNED);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.NOT_CERTIFIED);
        }
        return user;
    }

    private Order requireParticipantOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.isParticipant(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return order;
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "订单状态无效");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private OrderDto enrichDto(Order order) {
        OrderDto dto = OrderDto.from(order);
        if (order.getDemandId() != null) {
            try {
                var demand = demandService.getDetail(order.getDemandId());
                if (demand != null) {
                    dto.setDemandTitle(demand.getTitle());
                    dto.setRewardAmount(demand.getRewardAmount());
                    dto.setRewardType(demand.getRewardType() != null ? demand.getRewardType().name() : null);
                }
            } catch (Exception ignored) {}
        }
        if (order.getPublisherId() != null) {
            userRepository.findById(order.getPublisherId())
                    .ifPresent(u -> {
                        dto.setPublisherName(u.getUsername());
                        dto.setPublisherAvatarUrl(u.getAvatarUrl());
                    });
        }
        if (order.getAcceptorId() != null) {
            userRepository.findById(order.getAcceptorId())
                    .ifPresent(u -> {
                        dto.setAcceptorName(u.getUsername());
                        dto.setAcceptorAvatarUrl(u.getAvatarUrl());
                    });
        }
        if (reviewRepository != null && order.getId() != null) {
            dto.setPublisherReviewed(order.getPublisherId() != null
                    && reviewRepository.findByOrderIdAndReviewerId(order.getId(), order.getPublisherId()).isPresent());
            dto.setAcceptorReviewed(order.getAcceptorId() != null
                    && reviewRepository.findByOrderIdAndReviewerId(order.getId(), order.getAcceptorId()).isPresent());
        } else {
            dto.setPublisherReviewed(false);
            dto.setAcceptorReviewed(false);
        }
        return dto;
    }

    private static final class DemandRepositoryDemandServiceAdapter implements DemandService {
        private final DemandRepository demandRepository;

        private DemandRepositoryDemandServiceAdapter(DemandRepository demandRepository) {
            this.demandRepository = demandRepository;
        }

        @Override
        public DemandDto createDemand(Long userId, com.example.backend.demand.dto.DemandCreateDto req) {
            throw new UnsupportedOperationException("createDemand is not supported in adapter");
        }

        @Override
        public com.example.backend.common.PageResult<DemandDto> search(com.example.backend.demand.dto.DemandSearchCriteria criteria, int page, int size) {
            throw new UnsupportedOperationException("search is not supported in adapter");
        }

        @Override
        public com.example.backend.common.PageResult<DemandDto> recommend(Long userId, int page, int size) {
            throw new UnsupportedOperationException("recommend is not supported in adapter");
        }

        @Override
        public DemandDto getDetail(Long demandId) {
            return DemandDto.from(demandRepository.findById(demandId).orElse(null));
        }

        @Override
        public void updateStatus(Long demandId, DemandStatus status) {
            demandRepository.findById(demandId).ifPresent(demand -> {
                demand.setStatus(status);
                demandRepository.save(demand);
            });
        }

        @Override
        public DemandDto update(Long demandId, Long userId, com.example.backend.demand.dto.DemandUpdateDto req) {
            throw new UnsupportedOperationException("update is not supported in adapter");
        }

        @Override
        public void cancel(Long demandId, Long userId) {
            throw new UnsupportedOperationException("cancel is not supported in adapter");
        }

        @Override
        public void delete(Long demandId, Long userId) {
            throw new UnsupportedOperationException("delete is not supported in adapter");
        }
    }
}
