package com.example.backend.chat.dto;

import com.example.backend.chat.entity.Conversation;
import com.example.backend.common.enums.ConversationStatus;
import com.example.backend.user.dto.UserPublicDto;

import java.time.LocalDateTime;

public class ConversationDto {

    private Long id;
    private UserPublicDto targetUser;
    private Long relatedDemandId;
    private String relatedDemandTitle;
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    private ConversationStatus status;
    private Boolean blockedByMe = Boolean.FALSE;
    private Boolean blockedByTarget = Boolean.FALSE;
    private Long lastReadMessageId;
    private Long peerLastReadMessageId;

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
        dto.setStatus(conversation.getStatus());
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

    public String getRelatedDemandTitle() {
        return relatedDemandTitle;
    }

    public void setRelatedDemandTitle(String relatedDemandTitle) {
        this.relatedDemandTitle = relatedDemandTitle;
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

    public ConversationStatus getStatus() {
        return status;
    }

    public void setStatus(ConversationStatus status) {
        this.status = status;
    }

    public Boolean getBlockedByMe() {
        return blockedByMe;
    }

    public void setBlockedByMe(Boolean blockedByMe) {
        this.blockedByMe = blockedByMe;
    }

    public Boolean getBlockedByTarget() {
        return blockedByTarget;
    }

    public void setBlockedByTarget(Boolean blockedByTarget) {
        this.blockedByTarget = blockedByTarget;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public Long getPeerLastReadMessageId() {
        return peerLastReadMessageId;
    }

    public void setPeerLastReadMessageId(Long peerLastReadMessageId) {
        this.peerLastReadMessageId = peerLastReadMessageId;
    }
}
