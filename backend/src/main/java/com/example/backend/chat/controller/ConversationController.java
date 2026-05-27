package com.example.backend.chat.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.chat.dto.ConversationDto;
import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.dto.MessageDto;
import com.example.backend.chat.dto.SendMessageDto;
import com.example.backend.chat.service.ConversationService;
import com.example.backend.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ApiResponse<ConversationDto> getOrCreate(@RequestBody CreateConversationDto request) {
        return ApiResponse.success(conversationService.getOrCreate(AuthContext.requireUserId(), request));
    }

    @GetMapping
    public ApiResponse<List<ConversationDto>> list() {
        return ApiResponse.success(conversationService.listConversations(AuthContext.requireUserId()));
    }

    @PostMapping("/{id}/messages")
    public ApiResponse<MessageDto> sendMessage(@PathVariable Long id, @RequestBody SendMessageDto request) {
        return ApiResponse.success(conversationService.sendMessage(id, AuthContext.requireUserId(), request));
    }

    @GetMapping("/{id}/messages")
    public ApiResponse<List<MessageDto>> getMessages(@PathVariable Long id,
                                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.success(conversationService.getMessages(id, AuthContext.requireUserId(), page, size));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        conversationService.markRead(id, AuthContext.requireUserId());
        return ApiResponse.success();
    }
}
