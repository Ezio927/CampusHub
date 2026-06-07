package com.example.backend.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.backend.common.enums.ConversationStatus;

import java.time.LocalDateTime;

@TableName("conversations")
public class Conversation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String conversationKey;
    private Long user1Id;
    private Long user2Id;
    private Long relatedDemandId;
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private Long unreadCount = 0L;
    private ConversationStatus status = ConversationStatus.NORMAL;
    private Long blockedByUserId;
    private Long user1LastReadMessageId = 0L;
    private Long user2LastReadMessageId = 0L;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConversationKey() { return conversationKey; }
    public void setConversationKey(String conversationKey) { this.conversationKey = conversationKey; }
    public Long getUser1Id() { return user1Id; }
    public void setUser1Id(Long user1Id) { this.user1Id = user1Id; }
    public Long getUser2Id() { return user2Id; }
    public void setUser2Id(Long user2Id) { this.user2Id = user2Id; }
    public Long getRelatedDemandId() { return relatedDemandId; }
    public void setRelatedDemandId(Long relatedDemandId) { this.relatedDemandId = relatedDemandId; }
    public String getLastMessageContent() { return lastMessageContent; }
    public void setLastMessageContent(String lastMessageContent) { this.lastMessageContent = lastMessageContent; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
    public ConversationStatus getStatus() { return status; }
    public void setStatus(ConversationStatus status) { this.status = status; }
    public Long getBlockedByUserId() { return blockedByUserId; }
    public void setBlockedByUserId(Long blockedByUserId) { this.blockedByUserId = blockedByUserId; }
    public Long getUser1LastReadMessageId() { return user1LastReadMessageId; }
    public void setUser1LastReadMessageId(Long user1LastReadMessageId) { this.user1LastReadMessageId = user1LastReadMessageId; }
    public Long getUser2LastReadMessageId() { return user2LastReadMessageId; }
    public void setUser2LastReadMessageId(Long user2LastReadMessageId) { this.user2LastReadMessageId = user2LastReadMessageId; }
}
