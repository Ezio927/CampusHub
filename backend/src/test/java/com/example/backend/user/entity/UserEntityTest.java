package com.example.backend.user.entity;

import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void shouldAddCreditScore() {
        User u = new User();
        u.setCreditScore(80);
        u.addCredit(10);
        assertEquals(90, u.getCreditScore());
    }

    @Test
    void shouldCapCreditAt100() {
        User u = new User();
        u.setCreditScore(95);
        u.addCredit(20);
        assertEquals(100, u.getCreditScore());
    }

    @Test
    void shouldDeductCreditScore() {
        User u = new User();
        u.setCreditScore(50);
        u.deductCredit(30);
        assertEquals(20, u.getCreditScore());
    }

    @Test
    void shouldFloorCreditAt0() {
        User u = new User();
        u.setCreditScore(5);
        u.deductCredit(20);
        assertEquals(0, u.getCreditScore());
    }

    @Test
    void shouldNotChangeCreditForZeroAmount() {
        User u = new User();
        u.setCreditScore(50);
        u.addCredit(0);
        u.deductCredit(0);
        assertEquals(50, u.getCreditScore());
    }

    @Test
    void shouldCheckCreditSufficiency() {
        User u = new User();
        u.setCreditScore(60);
        assertTrue(u.isCreditSufficient());
        u.setCreditScore(59);
        assertFalse(u.isCreditSufficient());
    }

    @Test
    void shouldAddPublisherRating() {
        User u = new User();
        u.setPublisherRating(4.0);
        u.setPublisherRatingCount(5);
        u.addPublisherRating(5);
        assertEquals(6, u.getPublisherRatingCount());
        assertTrue(u.getPublisherRating() > 4.0);
    }

    @Test
    void shouldRejectInvalidRatingRange() {
        User u = new User();
        u.setPublisherRating(4.0);
        u.setPublisherRatingCount(1);
        u.addPublisherRating(0);
        u.addPublisherRating(6);
        assertEquals(1, u.getPublisherRatingCount());
    }

    @Test
    void shouldAddAcceptorRating() {
        User u = new User();
        u.addAcceptorRating(4);
        assertEquals(1, u.getAcceptorRatingCount());
        assertEquals(4.0, u.getAcceptorRating());
    }

    @Test
    void shouldManageQuotaAvailable() {
        User u = new User();
        u.addQuotaAvailable(20);
        assertEquals(20, u.getQuotaAvailable());
        u.deductQuotaAvailable(5);
        assertEquals(15, u.getQuotaAvailable());
        u.deductQuotaAvailable(100);
        assertEquals(0, u.getQuotaAvailable());
    }

    @Test
    void shouldFreezeAndReleaseQuota() {
        User u = new User();
        u.setQuotaAvailable(50);
        u.freezeQuota(20);
        assertEquals(30, u.getQuotaAvailable());
        assertEquals(20, u.getQuotaFrozen());
        u.releaseQuota(15);
        assertEquals(45, u.getQuotaAvailable());
        assertEquals(5, u.getQuotaFrozen());
        u.releaseQuota(100);
        assertEquals(50, u.getQuotaAvailable());
        assertEquals(0, u.getQuotaFrozen());
    }

    @Test
    void shouldConsumeFrozenQuota() {
        User u = new User();
        u.setQuotaFrozen(30);
        u.consumeFrozenQuota(10);
        assertEquals(20, u.getQuotaFrozen());
    }

    @Test
    void shouldHaveDefaultValues() {
        User u = new User();
        assertEquals(Integer.valueOf(100), u.getCreditScore());
        assertEquals(Double.valueOf(0.0), u.getPublisherRating());
        assertEquals(UserRole.STUDENT, u.getRole());
        assertEquals(UserStatus.PENDING_CERT, u.getStatus());
    }
}
