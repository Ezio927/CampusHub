package com.example.backend.demand.pattern.observer;

import com.example.backend.common.enums.OrderStatus;
import com.example.backend.user.service.CreditService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CreditScoreListener {

    private final CreditService creditService;

    public CreditScoreListener(CreditService creditService) {
        this.creditService = creditService;
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.getToStatus() == OrderStatus.COMPLETED) {
            creditService.addCredit(event.getAcceptorId(), 5, "Order completed", event.getOrderId());
        }
    }
}
