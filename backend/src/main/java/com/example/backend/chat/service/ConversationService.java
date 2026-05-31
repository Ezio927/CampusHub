package com.example.backend.chat.service;

import com.example.backend.chat.dto.ConversationDto;
import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.dto.MessageDto;
import com.example.backend.chat.dto.SendMessageDto;
import java.util.List;

public interface ConversationService {

    ConversationDto getOrCreate(Long userId, CreateConversationDto req);

    ConversationDto getConversation(Long conversationId, Long userId);

    List<ConversationDto> listConversations(Long userId);

    long countUnread(Long userId);

    MessageDto sendMessage(Long conversationId, Long senderId, SendMessageDto req);

    List<MessageDto> getMessages(Long conversationId, Long userId, int page, int size, Long afterId);

    List<MessageDto> searchMessages(Long conversationId, Long userId, String keyword);

    void markRead(Long conversationId, Long userId, Long lastReadMessageId);

    ConversationDto blockConversation(Long conversationId, Long userId);

    ConversationDto unblockConversation(Long conversationId, Long userId);

    MessageDto recallMessage(Long conversationId, Long messageId, Long userId);
}
