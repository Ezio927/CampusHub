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
import com.example.backend.common.enums.ConversationStatus;
import com.example.backend.common.enums.MessageStatus;
import com.example.backend.common.enums.MessageType;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.common.moderation.ContentModerationService;
import com.example.backend.common.moderation.ModerationResult;
import com.example.backend.common.validation.ImageDataUrlValidator;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {

    private static final int MAX_TEXT_LENGTH = 500;
    private static final int RECALL_LIMIT_MINUTES = 2;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final DemandRepository demandRepository;
    private final NotificationService notificationService;
    private final ContentModerationService contentModerationService;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   MessageRepository messageRepository,
                                   UserRepository userRepository,
                                   DemandRepository demandRepository,
                                   NotificationService notificationService,
                                   ContentModerationService contentModerationService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.demandRepository = demandRepository;
        this.notificationService = notificationService;
        this.contentModerationService = contentModerationService;
    }

    @Override
    public ConversationDto getOrCreate(Long userId, CreateConversationDto req) {
        if (req == null || req.getTargetUserId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "目标用户不能为空");
        }
        if (userId == null || userId.equals(req.getTargetUserId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能与自己创建会话");
        }
        requireUser(userId);
        requireUser(req.getTargetUserId());
        Demand relatedDemand = req.getDemandId() == null ? null : requireDemand(req.getDemandId());

        Long left = Math.min(userId, req.getTargetUserId());
        Long right = Math.max(userId, req.getTargetUserId());
        String key = ConversationRepository.buildKey(left, right);
        Conversation conversation = conversationRepository.findByConversationKey(key)
                .or(() -> conversationRepository.findByUser1IdAndUser2Id(left, right))
                .orElseGet(() -> {
                    Conversation created = new Conversation();
                    created.setConversationKey(key);
                    created.setUser1Id(left);
                    created.setUser2Id(right);
                    created.setRelatedDemandId(req.getDemandId());
                    created.setUnreadCount(0L);
                    created.setStatus(ConversationStatus.NORMAL);
                    created.setUser1LastReadMessageId(0L);
                    created.setUser2LastReadMessageId(0L);
                    return conversationRepository.save(created);
                });

        boolean changed = false;
        if (conversation.getConversationKey() == null) {
            conversation.setConversationKey(key);
            changed = true;
        }
        if (relatedDemand != null && !relatedDemand.getId().equals(conversation.getRelatedDemandId())) {
            conversation.setRelatedDemandId(relatedDemand.getId());
            changed = true;
        }
        if (changed) {
            conversationRepository.save(conversation);
        }
        return toDto(conversation, userId);
    }

    @Override
    public ConversationDto getConversation(Long conversationId, Long userId) {
        Conversation conversation = requireConversation(conversationId);
        ensureParticipant(conversation, userId);
        return toDto(conversation, userId);
    }

    @Override
    public List<ConversationDto> listConversations(Long userId) {
        requireUser(userId);
        return conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId)
                .stream()
                .map(conversation -> toDto(conversation, userId))
                .collect(Collectors.toList());
    }

    @Override
    public long countUnread(Long userId) {
        return listConversations(userId).stream()
                .map(ConversationDto::getUnreadCount)
                .filter(count -> count != null)
                .mapToLong(Long::longValue)
                .sum();
    }

    @Override
    public MessageDto sendMessage(Long conversationId, Long senderId, SendMessageDto req) {
        Conversation conversation = requireConversation(conversationId);
        ensureParticipant(conversation, senderId);
        requireCanSend(senderId);
        ensureConversationAllowsSend(conversation, senderId);

        MessageType type = req == null || req.getMessageType() == null ? MessageType.TEXT : req.getMessageType();
        String content = req == null ? null : req.getContent();
        String clientMessageId = normalizeClientMessageId(req == null ? null : req.getClientMessageId());
        if (clientMessageId != null) {
            Message existing = messageRepository.findBySenderIdAndClientMessageId(senderId, clientMessageId)
                    .orElse(null);
            if (existing != null) {
                if (!conversationId.equals(existing.getConversationId())) {
                    throw new BusinessException(ErrorCode.CONFLICT, "客户端消息ID已用于其他会话");
                }
                return toMessageDto(existing, conversation, senderId);
            }
        }

        ModerationResult moderationResult = validateMessageContent(type, content);
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setClientMessageId(clientMessageId);
        message.setContent(content.trim());
        message.setMessageType(type);
        message.setStatus(MessageStatus.SENT);
        applyQuote(message, req == null ? null : req.getQuotedMessageId(), conversationId);
        message.setRead(Boolean.FALSE);
        messageRepository.save(message);

        conversation.setLastMessageContent(lastMessagePreview(message));
        conversation.setLastMessageAt(message.getCreatedAt());
        conversation.setUnreadCount(countUnreadForUser(conversation, otherUserId(conversation, senderId)));
        conversationRepository.save(conversation);

        Long otherUserId = otherUserId(conversation, senderId);
        notificationService.createNotification(otherUserId, NotificationType.SYSTEM_ANNOUNCEMENT,
                "新私信", lastMessagePreview(message), conversationId);
        MessageDto dto = toMessageDto(message, conversation, senderId);
        dto.setRiskWarnings(moderationResult.getWarnings());
        return dto;
    }

    @Override
    public MessageDto appendSystemMessage(Long conversationId, String content, Long actorId) {
        Conversation conversation = requireConversation(conversationId);
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统消息不能为空");
        }
        String trimmed = truncate(content.trim(), MAX_TEXT_LENGTH);
        Long previousLastMessageId = messageRepository.findLastByConversationId(conversationId)
                .map(Message::getId)
                .orElse(0L);

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(0L);
        message.setContent(trimmed);
        message.setMessageType(MessageType.SYSTEM);
        message.setStatus(MessageStatus.SENT);
        message.setRead(Boolean.FALSE);
        messageRepository.save(message);

        if (actorId != null && isParticipant(conversation, actorId)
                && lastReadMessageId(conversation, actorId) >= previousLastMessageId) {
            setLastReadMessageId(conversation, actorId, message.getId());
        }
        conversation.setLastMessageContent(lastMessagePreview(message));
        conversation.setLastMessageAt(message.getCreatedAt());
        Long unreadUserId = actorId != null && isParticipant(conversation, actorId)
                ? otherUserId(conversation, actorId)
                : conversation.getUser1Id();
        conversation.setUnreadCount(countUnreadForUser(conversation, unreadUserId));
        conversationRepository.save(conversation);
        return toMessageDto(message, conversation, actorId);
    }

    @Override
    public List<MessageDto> getMessages(Long conversationId, Long userId, int page, int size, Long afterId) {
        Conversation conversation = requireConversation(conversationId);
        ensureParticipant(conversation, userId);
        int normalizedSize = Math.min(Math.max(1, size), 100);
        List<Message> messages;
        if (afterId != null && afterId > 0) {
            messages = messageRepository.findByConversationIdAndIdGreaterThanOrderByIdAsc(conversationId, afterId);
            if (messages.size() > normalizedSize) {
                messages = messages.subList(0, normalizedSize);
            }
        } else {
            List<Message> desc = messageRepository.findByConversationIdOrderByIdDesc(conversationId);
            int from = Math.max(0, page) * normalizedSize;
            int to = Math.min(desc.size(), from + normalizedSize);
            if (from >= desc.size()) {
                return new ArrayList<MessageDto>();
            }
            messages = new ArrayList<Message>(desc.subList(from, to));
            Collections.reverse(messages);
        }
        return messages.stream()
                .map(message -> toMessageDto(message, conversation, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageDto> searchMessages(Long conversationId, Long userId, String keyword) {
        Conversation conversation = requireConversation(conversationId);
        ensureParticipant(conversation, userId);
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<MessageDto>();
        }
        return messageRepository.searchByConversationIdAndKeyword(conversationId, keyword.trim())
                .stream()
                .filter(message -> message.getStatus() != MessageStatus.RECALLED)
                .map(message -> toMessageDto(message, conversation, userId))
                .collect(Collectors.toList());
    }

    @Override
    public void markRead(Long conversationId, Long userId, Long lastReadMessageId) {
        Conversation conversation = requireConversation(conversationId);
        ensureParticipant(conversation, userId);
        Long readMessageId = resolveReadMessageId(conversationId, lastReadMessageId);
        setLastReadMessageId(conversation, userId, readMessageId);

        List<Message> messages = messageRepository.findByConversationIdOrderByIdAsc(conversationId);
        for (Message message : messages) {
            if (!message.getSenderId().equals(userId) && message.getId() <= readMessageId) {
                message.setRead(Boolean.TRUE);
                messageRepository.save(message);
            }
        }
        conversation.setUnreadCount(countUnreadForUser(conversation, userId));
        conversationRepository.save(conversation);
    }

    @Override
    public ConversationDto blockConversation(Long conversationId, Long userId) {
        Conversation conversation = requireConversation(conversationId);
        ensureParticipant(conversation, userId);
        if (conversation.getBlockedByUserId() != null && !conversation.getBlockedByUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前会话已被对方拉黑");
        }
        conversationRepository.updateBlockState(conversation, ConversationStatus.BLOCKED, userId);
        return toDto(conversation, userId);
    }

    @Override
    public ConversationDto unblockConversation(Long conversationId, Long userId) {
        Conversation conversation = requireConversation(conversationId);
        ensureParticipant(conversation, userId);
        if (conversation.getBlockedByUserId() != null && !conversation.getBlockedByUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能由拉黑方解除拉黑");
        }
        conversationRepository.updateBlockState(conversation, ConversationStatus.NORMAL, null);
        return toDto(conversation, userId);
    }

    @Override
    public MessageDto recallMessage(Long conversationId, Long messageId, Long userId) {
        Conversation conversation = requireConversation(conversationId);
        ensureParticipant(conversation, userId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "消息不存在"));
        if (!conversationId.equals(message.getConversationId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息不属于当前会话");
        }
        if (!userId.equals(message.getSenderId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能撤回自己发送的消息");
        }
        if (message.getCreatedAt() == null
                || message.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(RECALL_LIMIT_MINUTES))) {
            throw new BusinessException(ErrorCode.CONFLICT, "消息只能在2分钟内撤回");
        }
        message.setStatus(MessageStatus.RECALLED);
        messageRepository.save(message);

        messageRepository.findLastByConversationId(conversationId).ifPresent(last -> {
            if (last.getId().equals(message.getId())) {
                conversation.setLastMessageContent(lastMessagePreview(message));
                conversationRepository.save(conversation);
            }
        });
        return toMessageDto(message, conversation, userId);
    }

    private ModerationResult validateMessageContent(MessageType type, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息内容不能为空");
        }
        if (type == MessageType.SYSTEM) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能发送系统消息");
        }
        if (type == MessageType.IMAGE) {
            ImageDataUrlValidator.requireValidImageDataUrl(content.trim(), "图片");
            return new ModerationResult();
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_TEXT_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "消息内容不能超过500个字符");
        }
        return contentModerationService.assertMessageAllowed(trimmed);
    }

    private ConversationDto toDto(Conversation conversation, Long userId) {
        ConversationDto dto = ConversationDto.from(conversation);
        Long targetUserId = otherUserId(conversation, userId);
        userRepository.findById(targetUserId)
                .ifPresent(user -> dto.setTargetUser(com.example.backend.user.dto.UserPublicDto.from(user)));
        if (conversation.getRelatedDemandId() != null) {
            demandRepository.findById(conversation.getRelatedDemandId())
                    .ifPresent(demand -> dto.setRelatedDemandTitle(demand.getTitle()));
        }
        Long lastRead = lastReadMessageId(conversation, userId);
        Long peerRead = peerLastReadMessageId(conversation, userId);
        dto.setLastReadMessageId(lastRead);
        dto.setPeerLastReadMessageId(peerRead);
        dto.setUnreadCount(countUnreadForUser(conversation, userId));
        dto.setBlockedByMe(conversation.getBlockedByUserId() != null && conversation.getBlockedByUserId().equals(userId));
        dto.setBlockedByTarget(conversation.getBlockedByUserId() != null && !conversation.getBlockedByUserId().equals(userId));
        if (dto.getStatus() == null) {
            dto.setStatus(ConversationStatus.NORMAL);
        }
        return dto;
    }

    private MessageDto toMessageDto(Message message, Conversation conversation, Long viewerId) {
        MessageDto dto = MessageDto.from(message);
        if (message.getStatus() == MessageStatus.RECALLED) {
            dto.setContent("消息已撤回");
            dto.setQuotedContent(null);
        }
        if (viewerId != null && viewerId.equals(message.getSenderId())) {
            Long peerRead = peerLastReadMessageId(conversation, viewerId);
            dto.setRead(message.getId() != null && peerRead != null && message.getId() <= peerRead);
        } else {
            dto.setRead(Boolean.TRUE);
        }
        if (dto.getStatus() == null) {
            dto.setStatus(MessageStatus.SENT);
        }
        return dto;
    }

    private Conversation requireConversation(Long conversationId) {
        if (conversationId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话ID不能为空");
        }
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "会话不存在"));
        if (conversation.getStatus() == null) {
            conversation.setStatus(ConversationStatus.NORMAL);
        }
        return conversation;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void requireCanSend(Long userId) {
        User sender = requireUser(userId);
        if (sender.getStatus() == UserStatus.BANNED) {
            throw new BusinessException(ErrorCode.ACCOUNT_BANNED);
        }
        if (sender.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.NOT_CERTIFIED, "账号未完成认证，暂不能发送私信");
        }
    }

    private Demand requireDemand(Long demandId) {
        return demandRepository.findById(demandId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEMAND_NOT_FOUND));
    }

    private void ensureParticipant(Conversation conversation, Long userId) {
        if (!isParticipant(conversation, userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean isParticipant(Conversation conversation, Long userId) {
        return userId != null
                && (conversation.getUser1Id().equals(userId) || conversation.getUser2Id().equals(userId));
    }

    private void ensureConversationAllowsSend(Conversation conversation, Long senderId) {
        if (conversation.getStatus() == ConversationStatus.MUTED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前会话已被禁言");
        }
        if (conversation.getStatus() == ConversationStatus.BLOCKED
                && conversation.getBlockedByUserId() != null
                && !conversation.getBlockedByUserId().equals(senderId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "对方已拉黑你，无法发送私信");
        }
    }

    private void applyQuote(Message message, Long quotedMessageId, Long conversationId) {
        if (quotedMessageId == null) {
            return;
        }
        Message quoted = messageRepository.findById(quotedMessageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "引用消息不存在"));
        if (!conversationId.equals(quoted.getConversationId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能引用当前会话内的消息");
        }
        message.setQuotedMessageId(quoted.getId());
        message.setQuotedContent(quoted.getMessageType() == MessageType.IMAGE
                ? "[图片]"
                : truncate(quoted.getContent(), 200));
    }

    private Long resolveReadMessageId(Long conversationId, Long requestedMessageId) {
        if (requestedMessageId != null) {
            Message message = messageRepository.findById(requestedMessageId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "消息不存在"));
            if (!conversationId.equals(message.getConversationId())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "消息不属于当前会话");
            }
            return requestedMessageId;
        }
        return messageRepository.findLastByConversationId(conversationId)
                .map(Message::getId)
                .orElse(0L);
    }

    private Long countUnreadForUser(Conversation conversation, Long userId) {
        return messageRepository.countUnreadAfterLastRead(
                conversation.getId(), userId, lastReadMessageId(conversation, userId));
    }

    private Long lastReadMessageId(Conversation conversation, Long userId) {
        Long value = conversation.getUser1Id().equals(userId)
                ? conversation.getUser1LastReadMessageId()
                : conversation.getUser2LastReadMessageId();
        return value == null ? 0L : value;
    }

    private Long peerLastReadMessageId(Conversation conversation, Long userId) {
        Long value = conversation.getUser1Id().equals(userId)
                ? conversation.getUser2LastReadMessageId()
                : conversation.getUser1LastReadMessageId();
        return value == null ? 0L : value;
    }

    private void setLastReadMessageId(Conversation conversation, Long userId, Long lastReadMessageId) {
        if (conversation.getUser1Id().equals(userId)) {
            conversation.setUser1LastReadMessageId(lastReadMessageId);
        } else {
            conversation.setUser2LastReadMessageId(lastReadMessageId);
        }
    }

    private Long otherUserId(Conversation conversation, Long userId) {
        return conversation.getUser1Id().equals(userId) ? conversation.getUser2Id() : conversation.getUser1Id();
    }

    private String normalizeClientMessageId(String clientMessageId) {
        if (clientMessageId == null || clientMessageId.trim().isEmpty()) {
            return null;
        }
        String value = clientMessageId.trim();
        if (value.length() > 80) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "客户端消息ID过长");
        }
        return value;
    }

    private String lastMessagePreview(Message message) {
        if (message.getStatus() == MessageStatus.RECALLED) {
            return "消息已撤回";
        }
        if (message.getMessageType() == MessageType.IMAGE) {
            return "[图片]";
        }
        return truncate(message.getContent(), 120);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
