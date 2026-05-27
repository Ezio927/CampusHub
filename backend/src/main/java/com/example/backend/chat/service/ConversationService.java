package com.example.backend.chat.service;

import com.example.backend.chat.dto.ConversationDto;
import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.dto.MessageDto;
import com.example.backend.chat.dto.SendMessageDto;
import com.example.backend.common.enums.MessageType;

import java.util.List;

public interface ConversationService {

    ConversationDto getOrCreate(Long userId, CreateConversationDto req);

    List<ConversationDto> listConversations(Long userId);

    MessageDto sendMessage(Long conversationId, Long senderId, SendMessageDto req);

    List<MessageDto> getMessages(Long conversationId, Long userId, int page, int size);

    void markRead(Long conversationId, Long userId);
}
