package com.example.backend.demand.pattern.observer;

import com.example.backend.common.enums.OrderStatus;
import com.example.backend.user.service.CreditService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CreditScoreListener {
    private final ObjectProvider<CreditService> creditServiceProvider;

    public CreditScoreListener(ObjectProvider<CreditService> creditServiceProvider) {
        this.creditServiceProvider = creditServiceProvider;
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        CreditService creditService = creditServiceProvider.getIfAvailable();
        if (creditService == null) {
            return;
        }
        if (event.getToStatus() == OrderStatus.COMPLETED) {
            creditService.addCredit(event.getAcceptorId(), 5, "完成订单加分", event.getOrderId());
        }
    }
}
