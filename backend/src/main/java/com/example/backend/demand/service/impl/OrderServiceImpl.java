package com.example.backend.demand.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.dto.AcceptOrderDto;
import com.example.backend.demand.dto.OrderCancelDto;
import com.example.backend.demand.dto.OrderDto;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.entity.OrderStatusLog;
import com.example.backend.demand.pattern.observer.OrderEventPublisher;
import com.example.backend.demand.pattern.strategy.OrderAction;
import com.example.backend.demand.pattern.strategy.OrderStateContext;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.repository.OrderStatusLogRepository;
import com.example.backend.demand.service.DemandService;
import com.example.backend.demand.service.OrderService;
import com.example.backend.user.service.CreditService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class OrderServiceImpl implements OrderService {
    private static final int FREE_CANCEL_MINUTES = 10;
    private static final int LATE_CANCEL_PENALTY = 10;

    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final OrderStateContext orderStateContext;
    private final OrderEventPublisher orderEventPublisher;
    private final ObjectProvider<DemandService> demandServiceProvider;
    private final ObjectProvider<CreditService> creditServiceProvider;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderStatusLogRepository orderStatusLogRepository,
            OrderStateContext orderStateContext,
            OrderEventPublisher orderEventPublisher,
            ObjectProvider<DemandService> demandServiceProvider,
            ObjectProvider<CreditService> creditServiceProvider) {
        this.orderRepository = orderRepository;
        this.orderStatusLogRepository = orderStatusLogRepository;
        this.orderStateContext = orderStateContext;
        this.orderEventPublisher = orderEventPublisher;
        this.demandServiceProvider = demandServiceProvider;
        this.creditServiceProvider = creditServiceProvider;
    }

    @Override
    @Transactional
    public OrderDto acceptDemand(Long userId, AcceptOrderDto request) {
        requireUserId(userId);
        Object demand = demandService().getDetail(request.getDemandId());
        if (demand == null) {
            throw new BusinessException(ErrorCode.DEMAND_NOT_FOUND);
        }
        Long demandId = readLong(demand, "id");
        Long publisherId = readLong(demand, "publisherId");
        if (publisherId == null) {
            publisherId = readNestedLong(demand, "publisher", "id");
        }
        DemandStatus demandStatus = readEnum(demand, "status", DemandStatus.class);
        if (publisherId == null || publisherId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能接自己发布的需求");
        }
        if (demandStatus != DemandStatus.OPEN) {
            throw new BusinessException(ErrorCode.DEMAND_ALREADY_ACCEPTED);
        }
        if (!creditService().checkCredit(userId)) {
            throw new BusinessException(ErrorCode.CREDIT_NOT_ENOUGH);
        }

        try {
            demandService().updateStatus(demandId, DemandStatus.ACCEPTED);
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.DEMAND_ALREADY_ACCEPTED);
        }

        Order order = new Order();
        order.setDemandId(demandId);
        order.setPublisherId(publisherId);
        order.setAcceptorId(userId);
        order.setStatus(OrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());
        order.setAutoConfirmDeadline(order.getAcceptedAt().plusHours(24));

        try {
            order = orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.DEMAND_ALREADY_ACCEPTED);
        }

        appendLogAndPublish(order, null, userId, "接单成功");
        return toDto(order, demand, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> listMyOrders(Long userId, String role, OrderStatus status, Pageable pageable) {
        requireUserId(userId);
        Page<Order> orders;
        if (role == null || role.isBlank()) {
            orders = orderRepository.findParticipatedOrders(userId, status, pageable);
        } else {
            String normalizedRole = role.toUpperCase(Locale.ROOT);
            if ("PUBLISHER".equals(normalizedRole)) {
                orders = status == null
                        ? orderRepository.findByPublisherId(userId, pageable)
                        : orderRepository.findByPublisherIdAndStatus(userId, status, pageable);
            } else if ("ACCEPTOR".equals(normalizedRole)) {
                orders = status == null
                        ? orderRepository.findByAcceptorId(userId, pageable)
                        : orderRepository.findByAcceptorIdAndStatus(userId, status, pageable);
            } else {
                throw new BusinessException(ErrorCode.PARAM_OUT_OF_RANGE, "role 只能为 PUBLISHER 或 ACCEPTOR");
            }
        }
        return orders.map(order -> toDto(order, safeDemand(order.getDemandId()), null));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getDetail(Long orderId, Long userId) {
        Order order = getParticipantOrder(orderId, userId);
        return toDto(order, safeDemand(order.getDemandId()), null);
    }

    @Override
    @Transactional
    public OrderDto markDelivered(Long orderId, Long userId) {
        Order order = getParticipantOrder(orderId, userId);
        if (!order.isAcceptor(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅服务方可标记送达");
        }
        orderStateContext.assertCanOperate(order, OrderAction.MARK_DELIVERED);
        OrderStatus fromStatus = order.getStatus();
        order.setStatus(OrderStatus.PENDING_CONFIRM);
        order.setAutoConfirmDeadline(LocalDateTime.now().plusHours(24));
        order = orderRepository.saveAndFlush(order);
        appendLogAndPublish(order, fromStatus, userId, "服务方标记送达");
        return toDto(order, safeDemand(order.getDemandId()), null);
    }

    @Override
    @Transactional
    public OrderDto confirmCompleted(Long orderId, Long userId) {
        Order order = getParticipantOrder(orderId, userId);
        if (!order.isPublisher(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅需求方可确认完成");
        }
        orderStateContext.assertCanOperate(order, OrderAction.CONFIRM_COMPLETED);
        OrderStatus fromStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order = orderRepository.saveAndFlush(order);
        appendLogAndPublish(order, fromStatus, userId, "需求方确认完成");
        return toDto(order, safeDemand(order.getDemandId()), null);
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(Long orderId, Long userId, OrderCancelDto request) {
        Order order = getParticipantOrder(orderId, userId);
        orderStateContext.assertCanOperate(order, OrderAction.CANCEL);
        int penalty = calculateCancelPenalty(order, LocalDateTime.now());
        if (penalty > 0) {
            creditService().deductCredit(userId, penalty, "超时取消订单扣分", order.getId());
        }

        OrderStatus fromStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(request.getReason());
        order = orderRepository.saveAndFlush(order);
        appendLogAndPublish(order, fromStatus, userId, request.getReason());
        return toDto(order, safeDemand(order.getDemandId()), -penalty);
    }

    @Override
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
            Order saved = orderRepository.saveAndFlush(order);
            appendLogAndPublish(saved, fromStatus, null, "系统 24 小时自动确认完成");
        }
    }

    // [Human-written] 接单后 10 分钟内取消不扣分，超过 10 分钟扣 10 分。
    public int calculateCancelPenalty(Order order, LocalDateTime now) {
        if (order.getAcceptedAt() == null) {
            return LATE_CANCEL_PENALTY;
        }
        LocalDateTime freeCancelDeadline = order.getAcceptedAt().plusMinutes(FREE_CANCEL_MINUTES);
        return now.isAfter(freeCancelDeadline) ? LATE_CANCEL_PENALTY : 0;
    }

    private Order getParticipantOrder(Long orderId, Long userId) {
        requireUserId(userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.isParticipant(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return order;
    }

    private void appendLogAndPublish(Order order, OrderStatus fromStatus, Long operatorId, String remark) {
        orderStatusLogRepository.save(OrderStatusLog.of(order.getId(), fromStatus, order.getStatus(), operatorId, remark));
        orderEventPublisher.publish(order, fromStatus, operatorId, remark);
    }

    private OrderDto toDto(Order order, Object demand, Integer creditPenalty) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setDemandId(order.getDemandId());
        dto.setPublisher(new OrderDto.UserSummary(order.getPublisherId()));
        dto.setAcceptor(new OrderDto.UserSummary(order.getAcceptorId()));
        dto.setStatus(order.getStatus());
        dto.setAcceptedAt(order.getAcceptedAt());
        dto.setCompletedAt(order.getCompletedAt());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setCancelReason(order.getCancelReason());
        dto.setAutoConfirmDeadline(order.getAutoConfirmDeadline());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setCreditPenalty(creditPenalty);

        if (demand != null) {
            dto.setDemandTitle(readString(demand, "title"));
            dto.setDemandDescription(readString(demand, "description"));
            dto.setCategory(readEnum(demand, "category", com.example.backend.common.enums.DemandCategory.class));
            dto.setRewardAmount(readBigDecimal(demand, "rewardAmount"));
            dto.setRewardType(readEnum(demand, "rewardType", com.example.backend.common.enums.RewardType.class));
        }
        if (order.getId() != null) {
            dto.setStatusLogs(orderStatusLogRepository.findByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                    .map(this::toLogDto)
                    .toList());
        }
        return dto;
    }

    private OrderDto.StatusLogDto toLogDto(OrderStatusLog log) {
        OrderDto.StatusLogDto dto = new OrderDto.StatusLogDto();
        dto.setFromStatus(log.getFromStatus());
        dto.setToStatus(log.getToStatus());
        dto.setOperatorId(log.getOperatorId());
        dto.setRemark(log.getRemark());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }

    private Object safeDemand(Long demandId) {
        DemandService demandService = demandServiceProvider.getIfAvailable();
        if (demandService == null || demandId == null) {
            return null;
        }
        try {
            return demandService.getDetail(demandId);
        } catch (BusinessException ignored) {
            return null;
        }
    }

    private DemandService demandService() {
        DemandService demandService = demandServiceProvider.getIfAvailable();
        if (demandService == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "DemandService 尚未接入，请先合并成员 B 的需求模块");
        }
        return demandService;
    }

    private CreditService creditService() {
        CreditService creditService = creditServiceProvider.getIfAvailable();
        if (creditService == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "CreditService 尚未接入，请先合并成员 B 的信用模块");
        }
        return creditService;
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private Object readValue(Object target, String property) {
        if (target == null || property == null || property.isBlank()) {
            return null;
        }
        String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
        try {
            Method getter = target.getClass().getMethod("get" + suffix);
            return getter.invoke(target);
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            Field field = target.getClass().getDeclaredField(property);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private Long readLong(Object target, String property) {
        Object value = readValue(target, property);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Long readNestedLong(Object target, String property, String nestedProperty) {
        return readLong(readValue(target, property), nestedProperty);
    }

    private String readString(Object target, String property) {
        Object value = readValue(target, property);
        return value == null ? null : value.toString();
    }

    private BigDecimal readBigDecimal(Object target, String property) {
        Object value = readValue(target, property);
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return null;
    }

    private <E extends Enum<E>> E readEnum(Object target, String property, Class<E> enumType) {
        Object value = readValue(target, property);
        if (enumType.isInstance(value)) {
            return enumType.cast(value);
        }
        if (value instanceof String text) {
            return Enum.valueOf(enumType, text);
        }
        return null;
    }
}
