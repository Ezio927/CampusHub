package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class PendingConfirmStrategy implements IOrderStateStrategy {
    private static final Set<OrderAction> ALLOWED = EnumSet.of(OrderAction.CONFIRM_COMPLETED, OrderAction.CANCEL);

    @Override
    public OrderStatus supportStatus() {
        return OrderStatus.PENDING_CONFIRM;
    }

    @Override
    public boolean canOperate(OrderAction action) {
        return ALLOWED.contains(action);
    }
}
