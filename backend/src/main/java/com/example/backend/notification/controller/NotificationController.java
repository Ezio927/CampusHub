package com.example.backend.notification.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.notification.dto.NotificationDto;
import com.example.backend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "通知", description = "通知列表、未读数量、已读标记")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "通知列表")
    @GetMapping
    public ApiResponse<List<NotificationDto>> list(
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.success(notificationService.listNotifications(AuthContext.requireUserId(), page, size));
    }

    @Operation(summary = "未读通知数量")
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.success(Map.of("unreadCount", notificationService.countUnread(AuthContext.requireUserId())));
    }

    @Operation(summary = "标记单条通知为已读")
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(
            @Parameter(description = "通知ID") @PathVariable Long id) {
        notificationService.markRead(id, AuthContext.requireUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "全部标记为已读")
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead(AuthContext.requireUserId());
        return ApiResponse.success();
    }
}
