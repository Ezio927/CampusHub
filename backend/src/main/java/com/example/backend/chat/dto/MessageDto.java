package com.example.backend.chat.dto;

import com.example.backend.chat.entity.Message;
import com.example.backend.common.enums.MessageStatus;
import com.example.backend.common.enums.MessageType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDto {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String clientMessageId;
    private String content;
    private MessageType messageType;
    private MessageStatus status;
    private Long quotedMessageId;
    private String quotedContent;
    private Boolean read;
    private LocalDateTime createdAt;
    private List<String> riskWarnings = new ArrayList<String>();

    public static MessageDto from(Message message) {
        if (message == null) {
            return null;
        }
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderId(message.getSenderId());
        dto.setClientMessageId(message.getClientMessageId());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setStatus(message.getStatus());
        dto.setQuotedMessageId(message.getQuotedMessageId());
        dto.setQuotedContent(message.getQuotedContent());
        dto.setRead(message.getRead());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getClientMessageId() {
        return clientMessageId;
    }

    public void setClientMessageId(String clientMessageId) {
        this.clientMessageId = clientMessageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public Long getQuotedMessageId() {
        return quotedMessageId;
    }

    public void setQuotedMessageId(Long quotedMessageId) {
        this.quotedMessageId = quotedMessageId;
    }

    public String getQuotedContent() {
        return quotedContent;
    }

    public void setQuotedContent(String quotedContent) {
        this.quotedContent = quotedContent;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getRiskWarnings() {
        return riskWarnings;
    }

    public void setRiskWarnings(List<String> riskWarnings) {
        this.riskWarnings = riskWarnings == null ? new ArrayList<String>() : riskWarnings;
    }
}
