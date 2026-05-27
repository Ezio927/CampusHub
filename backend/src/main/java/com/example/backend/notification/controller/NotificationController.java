package com.example.backend.notification.controller;

import com.example.backend.common.ApiResponse;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.notification.dto.NotificationDto;
import com.example.backend.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<Page<NotificationDto>> list(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Principal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(notificationService.list(resolveUserId(headerUserId, principal), pageable));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Principal principal) {
        return ApiResponse.success(Map.of("unreadCount", notificationService.unreadCount(resolveUserId(headerUserId, principal))));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<NotificationDto> markRead(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Principal principal,
            @PathVariable Long id) {
        return ApiResponse.success(notificationService.markRead(id, resolveUserId(headerUserId, principal)));
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Principal principal) {
        notificationService.markAllRead(resolveUserId(headerUserId, principal));
        return ApiResponse.success("已全部标记为已读", null);
    }

    private Long resolveUserId(Long headerUserId, Principal principal) {
        if (headerUserId != null) {
            return headerUserId;
        }
        if (principal != null) {
            try {
                return Long.valueOf(principal.getName());
            } catch (NumberFormatException ignored) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "无法识别当前登录用户");
            }
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}
