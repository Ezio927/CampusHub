package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.entity.Order;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderStateContext {
    private final Map<OrderStatus, IOrderStateStrategy> strategyMap = new EnumMap<>(OrderStatus.class);

    public OrderStateContext(List<IOrderStateStrategy> strategies) {
        for (IOrderStateStrategy strategy : strategies) {
            strategyMap.put(strategy.supportStatus(), strategy);
        }
    }

    // [Human-written] 根据订单当前状态找到对应策略，再判断这个操作能不能做。
    public boolean canOperate(Order order, OrderAction action) {
        if (order == null || order.getStatus() == null || action == null) {
            return false;
        }
        IOrderStateStrategy strategy = strategyMap.get(order.getStatus());
        return strategy != null && strategy.canOperate(action);
    }

    public void assertCanOperate(Order order, OrderAction action) {
        if (order != null && order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.ORDER_CANCELLED);
        }
        if (!canOperate(order, action)) {
            throw new BusinessException(ErrorCode.STATUS_NOT_ALLOWED);
        }
    }
}
