package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.enums.OrderStatus;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.demand.entity.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderStateContext {

    private final Map<OrderStatus, IOrderStateStrategy> strategyMap = new HashMap<OrderStatus, IOrderStateStrategy>();

    public OrderStateContext() {
        register(new PendingAcceptStrategy());
        register(new AcceptedStrategy());
        register(new InProgressStrategy());
        register(new PendingConfirmStrategy());
        register(new CompletedStrategy());
        register(new CancelledStrategy());
    }

    public OrderStateContext(List<IOrderStateStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "订单策略不能为空");
        }
        for (IOrderStateStrategy strategy : strategies) {
            register(strategy);
        }
    }

    public IOrderStateStrategy getStrategy(OrderStatus status) {
        return strategyMap.get(status);
    }

    public void assertCanTransition(Order order, OrderStatus nextStatus) {
        if (order == null || order.getStatus() == null || nextStatus == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "订单状态转换无效");
        }
        IOrderStateStrategy strategy = getStrategy(order.getStatus());
        if (strategy == null || !strategy.nextStatuses().contains(nextStatus)) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前状态不允许此操作");
        }
    }

    // [Human-written] 把操作映射成目标状态后，用状态策略统一校验是否合法。
    public void assertCanOperate(Order order, OrderAction action) {
        if (action == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "操作不能为空");
        }
        OrderStatus nextStatus = switch (action) {
            case ACCEPT -> OrderStatus.ACCEPTED;
            case MARK_DELIVERED -> OrderStatus.PENDING_CONFIRM;
            case CONFIRM_COMPLETED -> OrderStatus.COMPLETED;
            case CANCEL -> OrderStatus.CANCELLED;
        };
        assertCanTransition(order, nextStatus);
    }

    public boolean canOperate(Order order, OrderAction action) {
        try {
            assertCanOperate(order, action);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }

    private void register(IOrderStateStrategy strategy) {
        for (OrderStatus status : allStatuses()) {
            if (strategy.supports(status)) {
                strategyMap.put(status, strategy);
            }
        }
    }

    private List<OrderStatus> allStatuses() {
        return Arrays.asList(OrderStatus.values());
    }
}
