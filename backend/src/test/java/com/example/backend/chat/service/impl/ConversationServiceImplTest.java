package com.example.backend.chat.service.impl;

import com.example.backend.chat.dto.ConversationDto;
import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.dto.MessageDto;
import com.example.backend.chat.dto.SendMessageDto;
import com.example.backend.chat.entity.Conversation;
import com.example.backend.chat.entity.Message;
import com.example.backend.chat.repository.ConversationRepository;
import com.example.backend.chat.repository.MessageRepository;
import com.example.backend.common.enums.MessageType;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.notification.entity.Notification;
import com.example.backend.notification.repository.NotificationRepository;
import com.example.backend.notification.service.impl.NotificationServiceImpl;
import com.example.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ConversationServiceImplTest {

    @Test
    void shouldCreateConversationSendMessageAndMarkRead() {
        UserRepository userRepository = new UserRepository();
        DemandRepository demandRepository = new DemandRepository();
        ConversationRepository conversationRepository = new ConversationRepository();
        MessageRepository messageRepository = new MessageRepository();
        NotificationRepository notificationRepository = new NotificationRepository();
        NotificationServiceImpl notificationService = new NotificationServiceImpl(notificationRepository);
        ConversationServiceImpl service = new ConversationServiceImpl(
                conversationRepository, messageRepository, userRepository, demandRepository, notificationService);

        CreateConversationDto createConversationDto = new CreateConversationDto();
        createConversationDto.setTargetUserId(2L);
        createConversationDto.setDemandId(100L);

        ConversationDto conversationDto = service.getOrCreate(1L, createConversationDto);
        Assertions.assertNotNull(conversationDto.getId());
        Assertions.assertNotNull(conversationDto.getTargetUser());
        Assertions.assertEquals("admin", conversationDto.getTargetUser().getUsername());

        SendMessageDto sendMessageDto = new SendMessageDto();
        sendMessageDto.setContent("hello");
        sendMessageDto.setMessageType(MessageType.TEXT);
        MessageDto messageDto = service.sendMessage(conversationDto.getId(), 1L, sendMessageDto);
        Assertions.assertEquals("hello", messageDto.getContent());

        Conversation conversation = conversationRepository.findById(conversationDto.getId()).orElseThrow(AssertionError::new);
        Assertions.assertEquals("hello", conversation.getLastMessageContent());
        Assertions.assertEquals(1L, conversation.getUnreadCount());

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(2L);
        Assertions.assertEquals(1, notifications.size());
        Assertions.assertEquals(NotificationType.SYSTEM_ANNOUNCEMENT, notifications.get(0).getType());
        Assertions.assertEquals("New message", notifications.get(0).getTitle());

        service.markRead(conversationDto.getId(), 2L);
        Conversation afterRead = conversationRepository.findById(conversationDto.getId()).orElseThrow(AssertionError::new);
        Assertions.assertEquals(0L, afterRead.getUnreadCount());

        Message storedMessage = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationDto.getId())
                .get(0);
        Assertions.assertTrue(Boolean.TRUE.equals(storedMessage.getRead()));
    }
}
