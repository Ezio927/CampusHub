package com.example.backend.demand.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.demand.dto.AcceptOrderDto;
import com.example.backend.demand.dto.OrderCancelDto;
import com.example.backend.demand.dto.OrderDto;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.entity.OrderStatusLog;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.repository.OrderStatusLogRepository;
import com.example.backend.demand.service.OrderService;
import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.service.ConversationService;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CreditService;
import com.example.backend.notification.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final DemandRepository demandRepository;
    private final UserRepository userRepository;
    private final CreditService creditService;
    private final ConversationService conversationService;
    private final NotificationService notificationService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderStatusLogRepository orderStatusLogRepository,
                            DemandRepository demandRepository,
                            UserRepository userRepository,
                            CreditService creditService,
                            ConversationService conversationService,
                            NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.orderStatusLogRepository = orderStatusLogRepository;
        this.demandRepository = demandRepository;
        this.userRepository = userRepository;
        this.creditService = creditService;
        this.conversationService = conversationService;
        this.notificationService = notificationService;
    }

    @Override
    public OrderDto acceptDemand(Long userId, AcceptOrderDto req) {
        User acceptor = requireActiveUser(userId);
        if (!creditService.checkCredit(userId)) {
            throw new BusinessException(ErrorCode.CREDIT_INSUFFICIENT);
        }
        if (req == null || req.getDemandId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "demandId cannot be blank");
        }

        Demand demand = demandRepository.findById(req.getDemandId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEMAND_NOT_FOUND));
        if (!demand.isOpen()) {
            throw new BusinessException(ErrorCode.DEMAND_ALREADY_ACCEPTED);
        }
        if (orderRepository.findByDemandId(demand.getId()).isPresent()) {
            throw new BusinessException(ErrorCode.DEMAND_ALREADY_ACCEPTED);
        }

        demand.markAsAccepted();
        demandRepository.save(demand);

        Order order = new Order();
        order.setDemandId(demand.getId());
        order.setPublisherId(demand.getPublisherId());
        order.setAcceptorId(acceptor.getId());
        order.setStatus(OrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());
        order.setAutoConfirmDeadline(LocalDateTime.now().plusHours(24));
        orderRepository.save(order);
        appendLog(order.getId(), null, order.getStatus(), userId, "accept demand");
        CreateConversationDto conversationReq = new CreateConversationDto();
        conversationReq.setTargetUserId(demand.getPublisherId());
        conversationReq.setDemandId(demand.getId());
        conversationService.getOrCreate(userId, conversationReq);
        notificationService.createNotification(demand.getPublisherId(), NotificationType.ORDER_ACCEPTED,
                "Demand accepted", "Your demand has been accepted.", order.getId());
        return OrderDto.from(order);
    }

    @Override
    public List<OrderDto> list(Long userId, String role, String status) {
        OrderStatus orderStatus = status == null ? null : OrderStatus.valueOf(status.toUpperCase());
        List<Order> orders;
        if ("ACCEPTOR".equalsIgnoreCase(role)) {
            orders = orderRepository.findByAcceptorIdAndStatus(userId, orderStatus);
        } else {
            orders = orderRepository.findByPublisherIdAndStatus(userId, orderStatus);
        }
        return orders.stream().map(OrderDto::from).collect(Collectors.toList());
    }

    @Override
    public OrderDto getDetail(Long orderId, Long userId) {
        Order order = requireVisibleOrder(orderId, userId);
        return OrderDto.from(order);
    }

    @Override
    public OrderDto deliver(Long orderId, Long userId) {
        Order order = requireParticipantOrder(orderId, userId);
        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new BusinessException(ErrorCode.CONFLICT, "order is not accepted");
        }
        Demand demand = demandRepository.findById(order.getDemandId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEMAND_NOT_FOUND));
        OrderStatus from = order.getStatus();
        order.setStatus(OrderStatus.PENDING_CONFIRM);
        order.setUpdatedAt(LocalDateTime.now());
        demand.setStatus(DemandStatus.IN_PROGRESS);
        demandRepository.save(demand);
        orderRepository.save(order);
        appendLog(order.getId(), from, order.getStatus(), userId, "deliver");
        Long otherUserId = userId.equals(order.getPublisherId()) ? order.getAcceptorId() : order.getPublisherId();
        notificationService.createNotification(otherUserId, NotificationType.SYSTEM_ANNOUNCEMENT,
                "Order delivered", "The order has been marked as delivered.", order.getId());
        return OrderDto.from(order);
    }

    @Override
    public OrderDto confirm(Long orderId, Long userId) {
        Order order = requireParticipantOrder(orderId, userId);
        if (order.getStatus() != OrderStatus.PENDING_CONFIRM) {
            throw new BusinessException(ErrorCode.CONFLICT, "order is not pending confirm");
        }
        Demand demand = demandRepository.findById(order.getDemandId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEMAND_NOT_FOUND));
        OrderStatus from = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        demand.setStatus(DemandStatus.COMPLETED);
        demandRepository.save(demand);
        orderRepository.save(order);
        appendLog(order.getId(), from, order.getStatus(), userId, "confirm");
        Long otherUserId = userId.equals(order.getPublisherId()) ? order.getAcceptorId() : order.getPublisherId();
        notificationService.createNotification(otherUserId, NotificationType.ORDER_COMPLETED,
                "Order completed", "The order has been completed.", order.getId());
        return OrderDto.from(order);
    }

    @Override
    public OrderDto cancel(Long orderId, Long userId, OrderCancelDto req) {
        Order order = requireParticipantOrder(orderId, userId);
        if (!order.canCancel()) {
            throw new BusinessException(ErrorCode.CONFLICT, "order cannot be cancelled");
        }
        OrderStatus from = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(req == null ? null : req.getReason());
        orderRepository.save(order);
        appendLog(order.getId(), from, order.getStatus(), userId, "cancel");
        Long otherUserId = userId.equals(order.getPublisherId()) ? order.getAcceptorId() : order.getPublisherId();
        notificationService.createNotification(otherUserId, NotificationType.ORDER_CANCELLED,
                "Order cancelled", "The order has been cancelled.", order.getId());
        return OrderDto.from(order);
    }

    private User requireActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.NOT_CERTIFIED);
        }
        return user;
    }

    private Order requireVisibleOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!userId.equals(order.getPublisherId()) && !userId.equals(order.getAcceptorId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return order;
    }

    private Order requireParticipantOrder(Long orderId, Long userId) {
        return requireVisibleOrder(orderId, userId);
    }

    private void appendLog(Long orderId, OrderStatus from, OrderStatus to, Long operatorId, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        orderStatusLogRepository.save(log);
    }
}
