package com.example.backend.user.entity;

import com.example.backend.common.enums.CertStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CertificationEntityTest {

    @Test
    void shouldStartAsPending() {
        Certification c = new Certification();
        assertEquals(CertStatus.PENDING, c.getStatus());
    }

    @Test
    void shouldApprove() {
        Certification c = new Certification();
        c.approve(1L);
        assertEquals(CertStatus.APPROVED, c.getStatus());
        assertEquals(1L, c.getReviewedBy());
        assertNotNull(c.getReviewedAt());
    }

    @Test
    void shouldRejectWithReason() {
        Certification c = new Certification();
        c.reject(2L, "图片不清晰");
        assertEquals(CertStatus.REJECTED, c.getStatus());
        assertEquals("图片不清晰", c.getRejectReason());
        assertEquals(2L, c.getReviewedBy());
    }
}
