package com.example.backend.chat.service.impl;

import com.example.backend.chat.dto.ConversationDto;
import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.dto.MessageDto;
import com.example.backend.chat.dto.SendMessageDto;
import com.example.backend.chat.entity.Conversation;
import com.example.backend.chat.entity.Message;
import com.example.backend.chat.repository.ConversationRepository;
import com.example.backend.chat.repository.MessageRepository;
import com.example.backend.chat.service.ConversationService;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.MessageType;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final DemandRepository demandRepository;
    private final NotificationService notificationService;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   MessageRepository messageRepository,
                                   UserRepository userRepository,
                                   DemandRepository demandRepository,
                                   NotificationService notificationService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.demandRepository = demandRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ConversationDto getOrCreate(Long userId, CreateConversationDto req) {
        if (req == null || req.getTargetUserId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "目标用户不能为空");
        }
        if (userId == null || userId.equals(req.getTargetUserId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能与自己创建会话");
        }
        userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        userRepository.findById(req.getTargetUserId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Long left = Math.min(userId, req.getTargetUserId());
        Long right = Math.max(userId, req.getTargetUserId());
        Conversation conversation = conversationRepository
                .findByUser1IdAndUser2IdAndRelatedDemandId(left, right, req.getDemandId())
                .orElseGet(() -> {
                    Conversation created = new Conversation();
                    created.setUser1Id(left);
                    created.setUser2Id(right);
                    created.setRelatedDemandId(req.getDemandId());
                    created.setUnreadCount(0L);
                    return conversationRepository.save(created);
                });
        return toDto(conversation, userId);
    }

    @Override
    public List<ConversationDto> listConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId)
                .stream()
                .map(conversation -> toDto(conversation, userId))
                .collect(Collectors.toList());
    }

    @Override
    public MessageDto sendMessage(Long conversationId, Long senderId, SendMessageDto req) {
        if (req == null || req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息内容不能为空");
        }
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        ensureParticipant(conversation, senderId);

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(req.getContent());
        message.setMessageType(req.getMessageType() == null ? MessageType.TEXT : req.getMessageType());
        message.setRead(Boolean.FALSE);
        messageRepository.save(message);

        conversation.setLastMessageContent(req.getContent());
        conversation.setLastMessageAt(message.getCreatedAt());
        conversation.setUnreadCount((conversation.getUnreadCount() == null ? 0L : conversation.getUnreadCount()) + 1);
        conversationRepository.save(conversation);

        Long otherUserId = conversation.getUser1Id().equals(senderId) ? conversation.getUser2Id() : conversation.getUser1Id();
        notificationService.createNotification(otherUserId, NotificationType.SYSTEM_ANNOUNCEMENT,
                "New message", req.getContent(), conversationId);
        return MessageDto.from(message);
    }

    @Override
    public List<MessageDto> getMessages(Long conversationId, Long userId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        ensureParticipant(conversation, userId);
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);
        int from = Math.max(0, page) * Math.max(1, size);
        int to = Math.min(messages.size(), from + Math.max(1, size));
        if (from >= messages.size()) {
            return new ArrayList<MessageDto>();
        }
        return messages.subList(from, to).stream().map(MessageDto::from).collect(Collectors.toList());
    }

    @Override
    public void markRead(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        ensureParticipant(conversation, userId);
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);
        for (Message message : messages) {
            if (!message.getSenderId().equals(userId)) {
                message.setRead(Boolean.TRUE);
                messageRepository.save(message);
            }
        }
        conversation.setUnreadCount(0L);
        conversationRepository.save(conversation);
    }

    private ConversationDto toDto(Conversation conversation, Long userId) {
        ConversationDto dto = ConversationDto.from(conversation);
        Long targetUserId = conversation.getUser1Id().equals(userId) ? conversation.getUser2Id() : conversation.getUser1Id();
        userRepository.findById(targetUserId).ifPresent(user -> dto.setTargetUser(com.example.backend.user.dto.UserPublicDto.from(user)));
        dto.setUnreadCount(messageRepository.countByConversationIdAndSenderIdNotAndReadFalse(conversation.getId(), userId));
        return dto;
    }

    private void ensureParticipant(Conversation conversation, Long userId) {
        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
