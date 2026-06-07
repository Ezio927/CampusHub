package com.example.backend.chat.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.chat.dto.ConversationDto;
import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.dto.MessageDto;
import com.example.backend.chat.dto.MessageReportCreateDto;
import com.example.backend.chat.dto.MessageReportDto;
import com.example.backend.chat.dto.SendMessageDto;
import com.example.backend.chat.service.ConversationService;
import com.example.backend.chat.service.MessageReportService;
import com.example.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "即时通讯", description = "创建会话、发送消息、查看历史消息")
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageReportService messageReportService;

    public ConversationController(ConversationService conversationService,
                                  MessageReportService messageReportService) {
        this.conversationService = conversationService;
        this.messageReportService = messageReportService;
    }

    @Operation(summary = "创建或获取会话")
    @PostMapping
    public ApiResponse<ConversationDto> getOrCreate(@RequestBody CreateConversationDto request) {
        return ApiResponse.success(conversationService.getOrCreate(AuthContext.requireUserId(), request));
    }

    @Operation(summary = "会话列表")
    @GetMapping
    public ApiResponse<List<ConversationDto>> list() {
        return ApiResponse.success(conversationService.listConversations(AuthContext.requireUserId()));
    }

    @Operation(summary = "未读私信总数")
    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.success(conversationService.countUnread(AuthContext.requireUserId()));
    }

    @Operation(summary = "查看我的黑名单")
    @GetMapping("/blocked")
    public ApiResponse<List<ConversationDto>> blockedList() {
        return ApiResponse.success(conversationService.listBlockedConversations(AuthContext.requireUserId()));
    }

    @Operation(summary = "会话详情")
    @GetMapping("/{id}")
    public ApiResponse<ConversationDto> detail(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        return ApiResponse.success(conversationService.getConversation(id, AuthContext.requireUserId()));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/{id}/messages")
    public ApiResponse<MessageDto> sendMessage(
            @Parameter(description = "会话ID") @PathVariable Long id,
            @RequestBody SendMessageDto request) {
        return ApiResponse.success(conversationService.sendMessage(id, AuthContext.requireUserId(), request));
    }

    @Operation(summary = "查看历史消息")
    @GetMapping("/{id}/messages")
    public ApiResponse<List<MessageDto>> getMessages(
            @Parameter(description = "会话ID") @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(value = "size", defaultValue = "20") int size,
            @Parameter(description = "增量拉取起始消息ID") @RequestParam(value = "afterId", required = false) Long afterId) {
        return ApiResponse.success(conversationService.getMessages(id, AuthContext.requireUserId(), page, size, afterId));
    }

    @Operation(summary = "搜索会话内消息")
    @GetMapping("/{id}/messages/search")
    public ApiResponse<List<MessageDto>> searchMessages(
            @Parameter(description = "会话ID") @PathVariable Long id,
            @Parameter(description = "关键词") @RequestParam(value = "keyword") String keyword) {
        return ApiResponse.success(conversationService.searchMessages(id, AuthContext.requireUserId(), keyword));
    }

    @Operation(summary = "标记会话为已读")
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(
            @Parameter(description = "会话ID") @PathVariable Long id,
            @Parameter(description = "已读到的最后一条消息ID") @RequestParam(value = "lastReadMessageId", required = false) Long lastReadMessageId) {
        conversationService.markRead(id, AuthContext.requireUserId(), lastReadMessageId);
        return ApiResponse.success();
    }

    @Operation(summary = "拉黑会话对方")
    @PutMapping("/{id}/block")
    public ApiResponse<ConversationDto> block(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        return ApiResponse.success(conversationService.blockConversation(id, AuthContext.requireUserId()));
    }

    @Operation(summary = "解除拉黑")
    @PutMapping("/{id}/unblock")
    public ApiResponse<ConversationDto> unblock(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        return ApiResponse.success(conversationService.unblockConversation(id, AuthContext.requireUserId()));
    }

    @Operation(summary = "撤回私信消息")
    @PutMapping("/{id}/messages/{messageId}/recall")
    public ApiResponse<MessageDto> recallMessage(
            @Parameter(description = "会话ID") @PathVariable Long id,
            @Parameter(description = "消息ID") @PathVariable Long messageId) {
        return ApiResponse.success(conversationService.recallMessage(id, messageId, AuthContext.requireUserId()));
    }

    @Operation(summary = "举报私信消息")
    @PostMapping("/{id}/messages/{messageId}/reports")
    public ApiResponse<MessageReportDto> reportMessage(
            @Parameter(description = "会话ID") @PathVariable Long id,
            @Parameter(description = "消息ID") @PathVariable Long messageId,
            @RequestBody MessageReportCreateDto request) {
        return ApiResponse.success(messageReportService.createReport(
                AuthContext.requireUserId(), id, messageId, request));
    }
}
