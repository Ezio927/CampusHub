package com.example.backend.notification.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.notification.dto.NotificationDto;
import com.example.backend.notification.entity.Notification;
import com.example.backend.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class NotificationServiceImplMoreTest {

    private NotificationRepository repository;
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        repository = new NotificationRepository();
        notificationService = new NotificationServiceImpl(repository);
    }

    @Test
    void shouldCreateNotification() {
        NotificationDto dto = notificationService.createNotification(
                1L, NotificationType.SYSTEM_ANNOUNCEMENT, "title", "content", null);

        Assertions.assertNotNull(dto);
        Assertions.assertNotNull(dto.getId());
        Assertions.assertEquals("title", dto.getTitle());
        Assertions.assertEquals("content", dto.getContent());
    }

    @Test
    void shouldListWithPagination() {
        for (int i = 0; i < 15; i++) {
            notificationService.createNotification(1L, NotificationType.SYSTEM_ANNOUNCEMENT,
                    "title-" + i, "content-" + i, null);
        }

        List<NotificationDto> page0 = notificationService.listNotifications(1L, 0, 5);
        Assertions.assertEquals(5, page0.size());

        List<NotificationDto> page1 = notificationService.listNotifications(1L, 1, 5);
        Assertions.assertEquals(5, page1.size());

        Assertions.assertNotEquals(page0.get(0).getId(), page1.get(0).getId());
    }

    @Test
    void shouldCountUnread() {
        notificationService.createNotification(1L, NotificationType.SYSTEM_ANNOUNCEMENT, "t1", "c", null);
        notificationService.createNotification(1L, NotificationType.SYSTEM_ANNOUNCEMENT, "t2", "c", null);

        Assertions.assertEquals(2, notificationService.countUnread(1L));
        Assertions.assertEquals(0, notificationService.countUnread(999L));
    }

    @Test
    void shouldMarkSingleAsRead() {
        NotificationDto dto = notificationService.createNotification(1L, NotificationType.SYSTEM_ANNOUNCEMENT, "t", "c", null);
        Assertions.assertEquals(1, notificationService.countUnread(1L));

        notificationService.markRead(dto.getId(), 1L);
        Assertions.assertEquals(0, notificationService.countUnread(1L));
    }

    @Test
    void shouldRejectMarkReadByAnotherUser() {
        NotificationDto dto = notificationService.createNotification(1L, NotificationType.SYSTEM_ANNOUNCEMENT, "t", "c", null);

        Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> notificationService.markRead(dto.getId(), 2L)).getCode());
    }

    @Test
    void shouldThrowWhenNotificationNotFound() {
        Assertions.assertEquals(ErrorCode.NOT_FOUND.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> notificationService.markRead(9999L, 1L)).getCode());
    }

    @Test
    void shouldMarkAllAsRead() {
        notificationService.createNotification(1L, NotificationType.SYSTEM_ANNOUNCEMENT, "t1", "c", null);
        notificationService.createNotification(1L, NotificationType.SYSTEM_ANNOUNCEMENT, "t2", "c", null);
        notificationService.createNotification(2L, NotificationType.SYSTEM_ANNOUNCEMENT, "t3", "c", null);

        Assertions.assertEquals(2, notificationService.countUnread(1L));

        notificationService.markAllRead(1L);
        Assertions.assertEquals(0, notificationService.countUnread(1L));
    }
}
