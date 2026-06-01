package com.example.backend.chat.service;

import com.example.backend.chat.dto.MessageReportCreateDto;
import com.example.backend.chat.dto.MessageReportDto;
import com.example.backend.chat.dto.MessageReportReviewDto;

import java.util.List;

public interface MessageReportService {

    MessageReportDto createReport(Long reporterId, Long conversationId, Long messageId, MessageReportCreateDto req);

    List<MessageReportDto> listPending();

    MessageReportDto review(Long reportId, Long adminId, MessageReportReviewDto req);
}
