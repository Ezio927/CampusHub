package com.example.backend.chat.dto;

import com.example.backend.common.enums.MessageType;

public class SendMessageDto {

    private String content;
    private MessageType messageType = MessageType.TEXT;
    private Long quotedMessageId;
    private String clientMessageId;

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

    public Long getQuotedMessageId() {
        return quotedMessageId;
    }

    public void setQuotedMessageId(Long quotedMessageId) {
        this.quotedMessageId = quotedMessageId;
    }

    public String getClientMessageId() {
        return clientMessageId;
    }

    public void setClientMessageId(String clientMessageId) {
        this.clientMessageId = clientMessageId;
    }
}
