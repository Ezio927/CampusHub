package com.example.backend.demand.pattern.observer;

import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.service.DemandService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DemandStatusListener {

    private final DemandService demandService;

    public DemandStatusListener(DemandService demandService) {
        this.demandService = demandService;
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        DemandStatus demandStatus = mapStatus(event);
        if (demandStatus != null) {
            demandService.updateStatus(event.getDemandId(), demandStatus);
        }
    }

    private DemandStatus mapStatus(OrderStatusChangedEvent event) {
        return switch (event.getToStatus()) {
            case ACCEPTED -> DemandStatus.ACCEPTED;
            case PENDING_CONFIRM -> DemandStatus.IN_PROGRESS;
            case COMPLETED -> DemandStatus.COMPLETED;
            case CANCELLED -> isAcceptorCancelBeforeDelivery(event) ? DemandStatus.OPEN : DemandStatus.CANCELLED;
            default -> null;
        };
    }

    private boolean isAcceptorCancelBeforeDelivery(OrderStatusChangedEvent event) {
        return event.getOperatorId() != null
                && event.getOperatorId().equals(event.getAcceptorId())
                && event.getFromStatus() == OrderStatus.ACCEPTED;
    }
}
