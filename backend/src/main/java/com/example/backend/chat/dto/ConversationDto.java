package com.example.backend.chat.dto;

import com.example.backend.chat.entity.Conversation;
import com.example.backend.user.dto.UserPublicDto;

import java.time.LocalDateTime;

public class ConversationDto {

    private Long id;
    private UserPublicDto targetUser;
    private Long relatedDemandId;
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;

    public static ConversationDto from(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        ConversationDto dto = new ConversationDto();
        dto.setId(conversation.getId());
        dto.setRelatedDemandId(conversation.getRelatedDemandId());
        dto.setLastMessageContent(conversation.getLastMessageContent());
        dto.setLastMessageAt(conversation.getLastMessageAt());
        dto.setUnreadCount(conversation.getUnreadCount());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserPublicDto getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(UserPublicDto targetUser) {
        this.targetUser = targetUser;
    }

    public Long getRelatedDemandId() {
        return relatedDemandId;
    }

    public void setRelatedDemandId(Long relatedDemandId) {
        this.relatedDemandId = relatedDemandId;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
