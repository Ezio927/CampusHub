package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.BusinessException;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.entity.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStateContextTest {
    private final OrderStateContext context = new OrderStateContext(List.of(
            new PendingAcceptStrategy(),
            new AcceptedStrategy(),
            new InProgressStrategy(),
            new PendingConfirmStrategy(),
            new CompletedStrategy(),
            new CancelledStrategy()
    ));

    @Test
    void acceptedOrderCanBeDeliveredOrCancelled() {
        Order order = orderWithStatus(OrderStatus.ACCEPTED);

        assertTrue(context.canOperate(order, OrderAction.MARK_DELIVERED));
        assertTrue(context.canOperate(order, OrderAction.CANCEL));
        assertFalse(context.canOperate(order, OrderAction.CONFIRM_COMPLETED));
    }

    @Test
    void pendingConfirmOnlyAllowsConfirmOrCancel() {
        Order order = orderWithStatus(OrderStatus.PENDING_CONFIRM);

        assertDoesNotThrow(() -> context.assertCanOperate(order, OrderAction.CONFIRM_COMPLETED));
        assertThrows(BusinessException.class, () -> context.assertCanOperate(order, OrderAction.MARK_DELIVERED));
    }

    @Test
    void cancelledOrderThrowsDedicatedBusinessException() {
        Order order = orderWithStatus(OrderStatus.CANCELLED);

        assertThrows(BusinessException.class, () -> context.assertCanOperate(order, OrderAction.CANCEL));
    }

    private Order orderWithStatus(OrderStatus status) {
        Order order = new Order();
        order.setStatus(status);
        return order;
    }
}
