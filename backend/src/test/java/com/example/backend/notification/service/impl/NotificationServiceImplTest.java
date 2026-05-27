package com.example.backend.notification.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.notification.dto.NotificationDto;
import com.example.backend.notification.entity.Notification;
import com.example.backend.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class NotificationServiceImplTest {

    @Test
    void shouldCreateListAndMarkNotifications() {
        NotificationRepository notificationRepository = new NotificationRepository();
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository);

        NotificationDto first = service.createNotification(1L, NotificationType.SYSTEM_ANNOUNCEMENT,
                "First", "first content", 10L);
        NotificationDto second = service.createNotification(1L, NotificationType.ORDER_ACCEPTED,
                "Second", "second content", 11L);
        service.createNotification(2L, NotificationType.CREDIT_CHANGED,
                "Other", "other content", 12L);

        LocalDateTime baseTime = LocalDateTime.now();
        notificationRepository.findById(first.getId()).orElseThrow(AssertionError::new).setCreatedAt(baseTime.minusSeconds(2));
        notificationRepository.findById(second.getId()).orElseThrow(AssertionError::new).setCreatedAt(baseTime.minusSeconds(1));

        List<NotificationDto> notifications = service.listNotifications(1L, 0, 10);
        Assertions.assertEquals(2, notifications.size());
        Assertions.assertEquals("Second", notifications.get(0).getTitle());
        Assertions.assertEquals("First", notifications.get(1).getTitle());
        Assertions.assertEquals(11L, notifications.get(0).getRelatedId());
        Assertions.assertEquals(2L, service.countUnread(1L));

        service.markRead(first.getId(), 1L);
        Assertions.assertEquals(1L, service.countUnread(1L));
        Notification savedFirst = notificationRepository.findById(first.getId()).orElseThrow(AssertionError::new);
        Assertions.assertTrue(Boolean.TRUE.equals(savedFirst.getRead()));

        service.markAllRead(1L);
        Assertions.assertEquals(0L, service.countUnread(1L));
        Notification savedSecond = notificationRepository.findById(second.getId()).orElseThrow(AssertionError::new);
        Assertions.assertTrue(Boolean.TRUE.equals(savedSecond.getRead()));
    }

    @Test
    void shouldRejectMarkingAnotherUsersNotification() {
        NotificationRepository notificationRepository = new NotificationRepository();
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository);

        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setType(NotificationType.SYSTEM_ANNOUNCEMENT);
        notification.setTitle("Hello");
        notification.setContent("World");
        notification.setRelatedId(99L);
        notification.setRead(Boolean.FALSE);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                () -> service.markRead(notification.getId(), 2L));
        Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
    }
}
