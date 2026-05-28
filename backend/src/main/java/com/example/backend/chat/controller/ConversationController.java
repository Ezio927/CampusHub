package com.example.backend.chat.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.chat.dto.ConversationDto;
import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.dto.MessageDto;
import com.example.backend.chat.dto.SendMessageDto;
import com.example.backend.chat.service.ConversationService;
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

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
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
            @Parameter(description = "每页数量") @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.success(conversationService.getMessages(id, AuthContext.requireUserId(), page, size));
    }

    @Operation(summary = "标记会话为已读")
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        conversationService.markRead(id, AuthContext.requireUserId());
        return ApiResponse.success();
    }
}
