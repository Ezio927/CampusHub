package com.example.backend.chat.service.impl;

import com.example.backend.chat.dto.MessageReportCreateDto;
import com.example.backend.chat.dto.MessageReportDto;
import com.example.backend.chat.dto.MessageReportReviewDto;
import com.example.backend.chat.entity.Conversation;
import com.example.backend.chat.entity.Message;
import com.example.backend.chat.entity.MessageReport;
import com.example.backend.chat.repository.ConversationRepository;
import com.example.backend.chat.repository.MessageReportRepository;
import com.example.backend.chat.repository.MessageRepository;
import com.example.backend.chat.service.MessageReportService;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.ratelimit.ActionRateLimitService;
import com.example.backend.common.ratelimit.RateLimitAction;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.common.enums.ReportStatus;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.MessageType;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageReportServiceImpl implements MessageReportService {

    private final MessageReportRepository messageReportRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActionRateLimitService actionRateLimitService;

    public MessageReportServiceImpl(MessageReportRepository messageReportRepository,
                                    ConversationRepository conversationRepository,
                                    MessageRepository messageRepository,
                                    UserRepository userRepository,
                                    NotificationService notificationService,
                                    ActionRateLimitService actionRateLimitService) {
        this.messageReportRepository = messageReportRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.actionRateLimitService = actionRateLimitService;
    }

    @Override
    public MessageReportDto createReport(Long reporterId, Long conversationId, Long messageId, MessageReportCreateDto req) {
        actionRateLimitService.checkAndRecord(reporterId, RateLimitAction.MESSAGE_REPORT);
        if (req == null || req.getCategory() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择举报类型");
        }
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        ensureParticipant(conversation, reporterId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "消息不存在"));
        if (!conversationId.equals(message.getConversationId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息不属于当前会话");
        }

        MessageReport report = new MessageReport();
        report.setMessageId(messageId);
        report.setConversationId(conversationId);
        report.setReporterId(reporterId);
        report.setTargetUserId(message.getSenderId());
        report.setCategory(req.getCategory());
        report.setReason(req.getReason());
        report.setEvidenceUrls(req.getEvidenceUrls());
        report.setMessageType(message.getMessageType());
        if (message.getMessageType() == MessageType.IMAGE) {
            report.setMessageSnapshot("[图片]");
            report.setMessageImageUrl(message.getContent());
        } else {
            report.setMessageSnapshot(message.getContent());
            report.setMessageImageUrl(null);
        }
        report.setStatus(ReportStatus.PENDING);
        messageReportRepository.save(report);
        notifyAdmins(report);
        return MessageReportDto.from(report);
    }

    @Override
    public List<MessageReportDto> listPending() {
        return messageReportRepository.findByStatusOrderByCreatedAtAsc(ReportStatus.PENDING)
                .stream()
                .map(MessageReportDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public MessageReportDto review(Long reportId, Long adminId, MessageReportReviewDto req) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (admin.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        MessageReport report = messageReportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "举报已处理");
        }
        boolean approve = req != null && Boolean.TRUE.equals(req.getApprove());
        report.setStatus(approve ? ReportStatus.RESOLVED : ReportStatus.DISMISSED);
        report.setResolution(req == null ? null : req.getResolution());
        report.setReviewedBy(adminId);
        report.setReviewedAt(LocalDateTime.now());
        messageReportRepository.save(report);
        notificationService.createNotification(report.getReporterId(), NotificationType.SYSTEM_ANNOUNCEMENT,
                "Message report reviewed", "Your message report has been reviewed.", report.getId());
        return MessageReportDto.from(report);
    }

    private void notifyAdmins(MessageReport report) {
        userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .forEach(admin -> notificationService.createNotification(
                        admin.getId(),
                        NotificationType.SYSTEM_ANNOUNCEMENT,
                        "New message report",
                        "A private message report is waiting for review.",
                        report.getId()));
    }

    private void ensureParticipant(Conversation conversation, Long userId) {
        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
