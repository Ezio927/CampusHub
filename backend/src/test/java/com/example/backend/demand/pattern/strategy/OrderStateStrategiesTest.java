package com.example.backend.demand.pattern.strategy;

import com.example.backend.common.BusinessException;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.entity.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class OrderStateStrategiesTest {

    private OrderStateContext context;

    @BeforeEach
    void setUp() {
        context = new OrderStateContext(Arrays.asList(
                new PendingAcceptStrategy(),
                new AcceptedStrategy(),
                new PendingConfirmStrategy(),
                new CompletedStrategy(),
                new CancelledStrategy()
        ));
    }

    @Test
    void pendingAcceptAllowsAcceptAndCancel() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING_ACCEPT);

        Assertions.assertTrue(context.canOperate(order, OrderAction.ACCEPT));
        Assertions.assertTrue(context.canOperate(order, OrderAction.CANCEL));
        Assertions.assertFalse(context.canOperate(order, OrderAction.MARK_DELIVERED));
        Assertions.assertFalse(context.canOperate(order, OrderAction.CONFIRM_COMPLETED));
    }

    @Test
    void acceptedAllowsDeliverAndCancel() {
        Order order = new Order();
        order.setStatus(OrderStatus.ACCEPTED);

        Assertions.assertTrue(context.canOperate(order, OrderAction.MARK_DELIVERED));
        Assertions.assertTrue(context.canOperate(order, OrderAction.CANCEL));
        Assertions.assertFalse(context.canOperate(order, OrderAction.ACCEPT));
    }

    @Test
    void pendingConfirmAllowsCompleteAndCancel() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING_CONFIRM);

        Assertions.assertTrue(context.canOperate(order, OrderAction.CONFIRM_COMPLETED));
        Assertions.assertTrue(context.canOperate(order, OrderAction.CANCEL));
        Assertions.assertFalse(context.canOperate(order, OrderAction.ACCEPT));
        Assertions.assertFalse(context.canOperate(order, OrderAction.MARK_DELIVERED));
    }

    @Test
    void completedIsTerminal() {
        Order order = new Order();
        order.setStatus(OrderStatus.COMPLETED);

        for (OrderAction action : OrderAction.values()) {
            Assertions.assertFalse(context.canOperate(order, action),
                    "COMPLETED should not allow " + action);
        }
    }

    @Test
    void cancelledIsTerminal() {
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELLED);

        Assertions.assertThrows(BusinessException.class,
                () -> context.assertCanOperate(order, OrderAction.CANCEL));

        for (OrderAction action : OrderAction.values()) {
            Assertions.assertFalse(context.canOperate(order, action),
                    "CANCELLED should not allow " + action);
        }
    }

    @Test
    void assertCanOperateShouldNotThrowForValidAction() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING_ACCEPT);

        Assertions.assertDoesNotThrow(() -> context.assertCanOperate(order, OrderAction.ACCEPT));
    }

    @Test
    void assertCanOperateShouldThrowForInvalidAction() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING_ACCEPT);

        Assertions.assertThrows(BusinessException.class,
                () -> context.assertCanOperate(order, OrderAction.CONFIRM_COMPLETED));
    }

    @Test
    void shouldTransitionBetweenStates() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING_ACCEPT);

        IOrderStateStrategy strategy = context.getStrategy(OrderStatus.PENDING_ACCEPT);
        Assertions.assertNotNull(strategy);
        Assertions.assertTrue(strategy.nextStatuses().contains(OrderStatus.ACCEPTED));

        context.assertCanTransition(order, OrderStatus.ACCEPTED);
        Assertions.assertThrows(BusinessException.class,
                () -> context.assertCanTransition(order, OrderStatus.COMPLETED));
    }

    @Test
    void allStrategiesShouldBeRegistered() {
        for (OrderStatus status : OrderStatus.values()) {
            Assertions.assertNotNull(context.getStrategy(status),
                    "Strategy should be registered for " + status);
        }
    }
}
