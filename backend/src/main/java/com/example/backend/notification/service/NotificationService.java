package com.example.backend.notification.service;

import com.example.backend.common.enums.NotificationType;
import com.example.backend.notification.dto.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    NotificationDto create(Long userId, NotificationType type, String title, String content, Long relatedId);

    Page<NotificationDto> list(Long userId, Pageable pageable);

    long unreadCount(Long userId);

    NotificationDto markRead(Long notificationId, Long userId);

    void markAllRead(Long userId);
}
