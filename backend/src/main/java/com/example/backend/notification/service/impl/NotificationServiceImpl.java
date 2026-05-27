package com.example.backend.notification.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.notification.dto.NotificationDto;
import com.example.backend.notification.entity.Notification;
import com.example.backend.notification.repository.NotificationRepository;
import com.example.backend.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public NotificationDto create(Long userId, NotificationType type, String title, String content, Long relatedId) {
        Notification notification = Notification.of(userId, type, title, content, relatedId);
        return toDto(notificationRepository.save(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> list(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationDto markRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "通知不存在"));
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        notification.setRead(true);
        return toDto(notification);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    private NotificationDto toDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setRelatedId(notification.getRelatedId());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
