package com.example.backend.chat.dto;

import com.example.backend.common.enums.MessageType;

public class SendMessageDto {

    private String content;
    private MessageType messageType = MessageType.TEXT;

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
}
