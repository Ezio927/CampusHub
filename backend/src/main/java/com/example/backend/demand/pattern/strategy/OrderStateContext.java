package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.enums.OrderStatus;
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

    public IOrderStateStrategy getStrategy(OrderStatus status) {
        return strategyMap.get(status);
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
