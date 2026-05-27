package com.example.backend.demand.service.impl;

import com.example.backend.demand.entity.Order;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderServiceImplTest {
    private final OrderServiceImpl orderService = new OrderServiceImpl(null, null, null, null, null, null);

    @Test
    void cancelWithinTenMinutesHasNoPenalty() {
        LocalDateTime acceptedAt = LocalDateTime.of(2026, 5, 27, 10, 0);
        Order order = new Order();
        order.setAcceptedAt(acceptedAt);

        assertEquals(0, orderService.calculateCancelPenalty(order, acceptedAt.plusMinutes(10)));
    }

    @Test
    void cancelAfterTenMinutesDeductsCredit() {
        LocalDateTime acceptedAt = LocalDateTime.of(2026, 5, 27, 10, 0);
        Order order = new Order();
        order.setAcceptedAt(acceptedAt);

        assertEquals(10, orderService.calculateCancelPenalty(order, acceptedAt.plusMinutes(10).plusSeconds(1)));
    }
}
