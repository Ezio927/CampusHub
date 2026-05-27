package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.enums.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class CancelledStrategy implements IOrderStateStrategy {
    @Override
    public OrderStatus supportStatus() {
        return OrderStatus.CANCELLED;
    }

    @Override
    public boolean canOperate(OrderAction action) {
        return false;
    }
}
