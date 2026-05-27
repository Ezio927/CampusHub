package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.enums.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class CompletedStrategy implements IOrderStateStrategy {
    @Override
    public OrderStatus supportStatus() {
        return OrderStatus.COMPLETED;
    }

    @Override
    public boolean canOperate(OrderAction action) {
        return false;
    }
}
