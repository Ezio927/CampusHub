package com.example.backend.demand.entity;

import com.example.backend.common.enums.DemandStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DemandEntityTest {

    @Test
    void shouldStartAsOpen() {
        Demand d = new Demand();
        assertTrue(d.isOpen());
        assertEquals(DemandStatus.OPEN, d.getStatus());
    }

    @Test
    void shouldTransitionToAccepted() {
        Demand d = new Demand();
        d.markAsAccepted();
        assertEquals(DemandStatus.ACCEPTED, d.getStatus());
    }

    @Test
    void shouldTransitionToCompleted() {
        Demand d = new Demand();
        d.markAsCompleted();
        assertEquals(DemandStatus.COMPLETED, d.getStatus());
    }

    @Test
    void shouldCancel() {
        Demand d = new Demand();
        d.cancel();
        assertEquals(DemandStatus.CANCELLED, d.getStatus());
        assertFalse(d.isOpen());
    }
}
