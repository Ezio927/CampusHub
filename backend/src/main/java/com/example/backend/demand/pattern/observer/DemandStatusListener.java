package com.example.backend.demand.pattern.observer;

import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.service.DemandService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DemandStatusListener {
    private final ObjectProvider<DemandService> demandServiceProvider;

    public DemandStatusListener(ObjectProvider<DemandService> demandServiceProvider) {
        this.demandServiceProvider = demandServiceProvider;
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.getFromStatus() == null && event.getToStatus() == OrderStatus.ACCEPTED) {
            return;
        }
        DemandService demandService = demandServiceProvider.getIfAvailable();
        if (demandService == null) {
            return;
        }
        DemandStatus demandStatus = toDemandStatus(event.getToStatus());
        if (demandStatus != null) {
            demandService.updateStatus(event.getDemandId(), demandStatus);
        }
    }

    private DemandStatus toDemandStatus(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case ACCEPTED -> DemandStatus.ACCEPTED;
            case IN_PROGRESS, PENDING_CONFIRM -> DemandStatus.IN_PROGRESS;
            case COMPLETED -> DemandStatus.COMPLETED;
            case CANCELLED -> DemandStatus.CANCELLED;
            default -> null;
        };
    }
}
