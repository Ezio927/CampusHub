package com.example.backend.notification.service;

import com.example.backend.common.enums.NotificationType;
import com.example.backend.notification.dto.NotificationDto;

import java.util.List;

public interface NotificationService {

    NotificationDto createNotification(Long userId, NotificationType type, String title, String content, Long relatedId);

    List<NotificationDto> listNotifications(Long userId, int page, int size);

    long countUnread(Long userId);

    void markRead(Long notificationId, Long userId);

    void markAllRead(Long userId);
}
