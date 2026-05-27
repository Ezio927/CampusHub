package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.enums.OrderStatus;

public interface IOrderStateStrategy {
    OrderStatus supportStatus();

    boolean canOperate(OrderAction action);
}
