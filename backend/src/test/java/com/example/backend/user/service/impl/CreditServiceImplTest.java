package com.example.backend.user.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.notification.repository.NotificationRepository;
import com.example.backend.notification.service.impl.NotificationServiceImpl;
import com.example.backend.user.dto.CreditRecordDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.CreditRecordRepository;
import com.example.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class CreditServiceImplTest {

    private UserRepository userRepository;
    private CreditServiceImpl creditService;
    private Long userId;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        CreditRecordRepository creditRecordRepository = new CreditRecordRepository();
        NotificationServiceImpl notificationService = new NotificationServiceImpl(new NotificationRepository());
        creditService = new CreditServiceImpl(userRepository, creditRecordRepository, notificationService);

        User alice = userRepository.findById(1L).orElseThrow();
        alice.setStatus(UserStatus.ACTIVE);
        userRepository.save(alice);
        userId = 1L;
    }

    @Test
    void shouldCheckCreditThreshold() {
        Assertions.assertTrue(creditService.checkCredit(userId));

        User alice = userRepository.findById(userId).orElseThrow();
        alice.setCreditScore(59);
        userRepository.save(alice);
        Assertions.assertFalse(creditService.checkCredit(userId));

        alice.setCreditScore(60);
        userRepository.save(alice);
        Assertions.assertTrue(creditService.checkCredit(userId));
    }

    @Test
    void shouldGetCreditScore() {
        Assertions.assertEquals(100, creditService.getCreditScore(userId));
    }

    @Test
    void shouldAddCreditAndCreateRecord() {
        CreditRecordDto record = creditService.addCredit(userId, 10, "bonus", null);
        Assertions.assertNotNull(record);
        Assertions.assertEquals(10, record.getChangeAmount());

        Assertions.assertEquals(110, creditService.getCreditScore(userId));

        List<CreditRecordDto> records = creditService.listRecords(userId);
        Assertions.assertEquals(1, records.size());
    }

    @Test
    void shouldRejectNonPositiveAmountForAddCredit() {
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> creditService.addCredit(userId, 0, "zero", null)).getCode());

        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> creditService.addCredit(userId, -5, "negative", null)).getCode());
    }

    @Test
    void shouldDeductCreditAndCreateRecord() {
        CreditRecordDto record = creditService.deductCredit(userId, 20, "penalty", 100L);
        Assertions.assertNotNull(record);
        Assertions.assertTrue(record.getChangeAmount() < 0);

        Assertions.assertEquals(80, creditService.getCreditScore(userId));
    }

    @Test
    void shouldRejectNonPositiveAmountForDeductCredit() {
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> creditService.deductCredit(userId, 0, "zero", null)).getCode());
    }

    @Test
    void shouldFloorCreditAtZero() {
        creditService.deductCredit(userId, 200, "heavy penalty", null);
        Assertions.assertEquals(0, creditService.getCreditScore(userId));
        Assertions.assertFalse(creditService.checkCredit(userId));
    }

    @Test
    void shouldListRecordsInDescOrder() {
        creditService.addCredit(userId, 5, "first", null);
        creditService.deductCredit(userId, 3, "second", null);
        creditService.addCredit(userId, 10, "third", null);

        List<CreditRecordDto> records = creditService.listRecords(userId);
        Assertions.assertEquals(3, records.size());
    }
}
