package com.example.backend.demand.entity;

import com.example.backend.common.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderEntityTest {

    @Test
    void shouldStartAsPendingAccept() {
        Order o = new Order();
        assertEquals(OrderStatus.PENDING_ACCEPT, o.getStatus());
    }

    @Test
    void shouldIdentifyParticipants() {
        Order o = new Order();
        o.setPublisherId(10L);
        o.setAcceptorId(20L);
        assertTrue(o.isParticipant(10L));
        assertTrue(o.isParticipant(20L));
        assertFalse(o.isParticipant(30L));
        assertTrue(o.isPublisher(10L));
        assertFalse(o.isPublisher(20L));
        assertTrue(o.isAcceptor(20L));
    }

    @Test
    void shouldHandleNullUserId() {
        Order o = new Order();
        o.setPublisherId(1L);
        assertFalse(o.isParticipant(null));
        assertFalse(o.isPublisher(null));
        assertFalse(o.isAcceptor(null));
    }

    @Test
    void shouldAllowCancelInPreConfirmStates() {
        Order o = new Order();
        o.setStatus(OrderStatus.PENDING_ACCEPT);
        assertTrue(o.canCancel());
        o.setStatus(OrderStatus.ACCEPTED);
        assertTrue(o.canCancel());
        o.setStatus(OrderStatus.PENDING_CONFIRM);
        assertTrue(o.canCancel());
        o.setStatus(OrderStatus.COMPLETED);
        assertFalse(o.canCancel());
        o.setStatus(OrderStatus.CANCELLED);
        assertFalse(o.canCancel());
    }

    @Test
    void shouldDetectAutoConfirmExpiry() {
        Order o = new Order();
        o.setAutoConfirmDeadline(LocalDateTime.now().minusMinutes(1));
        assertTrue(o.isAutoConfirmExpired());
        o.setAutoConfirmDeadline(LocalDateTime.now().plusHours(24));
        assertFalse(o.isAutoConfirmExpired());
    }
}
