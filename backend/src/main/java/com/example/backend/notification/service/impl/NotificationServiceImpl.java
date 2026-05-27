package com.example.backend.notification.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.notification.dto.NotificationDto;
import com.example.backend.notification.entity.Notification;
import com.example.backend.notification.repository.NotificationRepository;
import com.example.backend.notification.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationDto createNotification(Long userId, NotificationType type, String title, String content, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRead(Boolean.FALSE);
        notificationRepository.save(notification);
        return NotificationDto.from(notification);
    }

    @Override
    public List<NotificationDto> listNotifications(Long userId, int page, int size) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        int from = Math.max(0, page) * Math.max(1, size);
        int to = Math.min(notifications.size(), from + Math.max(1, size));
        if (from >= notifications.size()) {
            return new ArrayList<NotificationDto>();
        }
        return notifications.subList(from, to).stream().map(NotificationDto::from).collect(Collectors.toList());
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public void markRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!userId.equals(notification.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        notification.setRead(Boolean.TRUE);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllRead(Long userId) {
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(notification -> {
                    notification.setRead(Boolean.TRUE);
                    notificationRepository.save(notification);
                });
    }
}
