package com.example.backend.demand.service.impl;

import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.service.ConversationService;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.DemandStatus;
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
import com.example.backend.demand.service.DemandService;
import com.example.backend.demand.service.OrderService;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    private final UserRepository userRepository;
    private final CreditService creditService;
    private final ConversationService conversationService;

    @Autowired
    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderStatusLogRepository orderStatusLogRepository,
            OrderStateContext orderStateContext,
            OrderEventPublisher orderEventPublisher,
            DemandService demandService,
            UserRepository userRepository,
            CreditService creditService,
            ConversationService conversationService) {
        this.orderRepository = orderRepository;
        this.orderStatusLogRepository = orderStatusLogRepository;
        this.orderStateContext = orderStateContext;
        this.orderEventPublisher = orderEventPublisher;
        this.demandService = demandService;
        this.userRepository = userRepository;
        this.creditService = creditService;
        this.conversationService = conversationService;
    }

    // [Human-written] 兼容旧测试构造签名：把 DemandRepository 适配成最小 DemandService 能力。
    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderStatusLogRepository orderStatusLogRepository,
            DemandRepository demandRepository,
            UserRepository userRepository,
            CreditService creditService,
            ConversationService conversationService,
            NotificationService notificationService) {
        this(
                orderRepository,
                orderStatusLogRepository,
                new OrderStateContext(),
                new OrderEventPublisher(event -> {
                }),
                new DemandRepositoryDemandServiceAdapter(demandRepository),
                userRepository,
                creditService,
                conversationService);
    }

    @Override
    public OrderDto acceptDemand(Long userId, AcceptOrderDto req) {
        User acceptor = requireActiveUser(userId);
        if (req == null || req.getDemandId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "需求ID不能为空");
        }
        if (!creditService.checkCredit(userId)) {
            throw new BusinessException(ErrorCode.CREDIT_INSUFFICIENT);
        }

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
        if (orderRepository.findByDemandId(demand.getId()).isPresent()) {
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
        orderRepository.save(order);

        appendLogAndPublish(order, null, userId, "accept demand");

        CreateConversationDto conversationReq = new CreateConversationDto();
        conversationReq.setTargetUserId(demand.getPublisherId());
        conversationReq.setDemandId(demand.getId());
        conversationService.getOrCreate(userId, conversationReq);

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
    public OrderDto getDetail(Long orderId, Long userId) {
        return enrichDto(requireParticipantOrder(orderId, userId));
    }

    @Override
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
        return enrichDto(order);
    }

    @Override
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

        appendLogAndPublish(order, fromStatus, userId, "confirm");
        return enrichDto(order);
    }

    @Override
    public OrderDto cancel(Long orderId, Long userId, OrderCancelDto req) {
        Order order = requireParticipantOrder(orderId, userId);
        orderStateContext.assertCanOperate(order, OrderAction.CANCEL);

        int penalty = calculateCancelPenalty(order, LocalDateTime.now());
        if (penalty > 0) {
            creditService.deductCredit(userId, penalty, "cancel order penalty", order.getId());
        }

        OrderStatus fromStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(req == null ? null : req.getReason());
        orderRepository.save(order);

        appendLogAndPublish(order, fromStatus, userId, order.getCancelReason());

        OrderDto dto = enrichDto(order);
        dto.setCreditPenalty(-penalty);
        return dto;
    }

    @Scheduled(fixedDelay = 600_000)
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
            appendLogAndPublish(order, fromStatus, null, "auto confirm completed");
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

    private User requireActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
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
                    .ifPresent(u -> dto.setPublisherName(u.getUsername()));
        }
        if (order.getAcceptorId() != null) {
            userRepository.findById(order.getAcceptorId())
                    .ifPresent(u -> dto.setAcceptorName(u.getUsername()));
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
        public List<DemandDto> search(com.example.backend.demand.dto.DemandSearchCriteria criteria) {
            throw new UnsupportedOperationException("search is not supported in adapter");
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
    }
}
