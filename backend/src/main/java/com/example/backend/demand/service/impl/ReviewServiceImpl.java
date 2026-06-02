package com.example.backend.demand.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.common.enums.ReviewerRole;
import com.example.backend.demand.dto.ReviewDto;
import com.example.backend.demand.dto.ReviewSubmitDto;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.entity.Review;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.repository.ReviewRepository;
import com.example.backend.demand.service.ArbitrationService;
import com.example.backend.demand.service.ReviewService;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.QuotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ArbitrationService arbitrationService;
    private final NotificationService notificationService;
    private final QuotaService quotaService;
    private final UserRepository userRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             OrderRepository orderRepository,
                             ArbitrationService arbitrationService,
                             NotificationService notificationService,
                             QuotaService quotaService,
                             UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.arbitrationService = arbitrationService;
        this.notificationService = notificationService;
        this.quotaService = quotaService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ReviewDto submitReview(Long userId, ReviewSubmitDto req) {
        if (req == null || req.getOrderId() == null || req.getRevieweeId() == null || req.getRating() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评价信息不完整");
        }
        if (req.getRating() < 1 || req.getRating() > 5) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评分必须在1到5之间");
        }
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() != OrderStatus.COMPLETED && order.getStatus() != OrderStatus.PENDING_CONFIRM) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单未完成，无法评价");
        }
        boolean reviewerIsPublisher = userId.equals(order.getPublisherId());
        boolean reviewerIsAcceptor = userId.equals(order.getAcceptorId());
        if (!reviewerIsPublisher && !reviewerIsAcceptor) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有订单参与者才能评价");
        }
        Long expectedRevieweeId = reviewerIsPublisher ? order.getAcceptorId() : order.getPublisherId();
        if (!req.getRevieweeId().equals(expectedRevieweeId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "被评价者必须是该订单的另一方");
        }
        reviewRepository.findByOrderIdAndReviewerId(req.getOrderId(), userId).ifPresent(review -> {
            throw new BusinessException(ErrorCode.REVIEW_DUPLICATED);
        });

        Review review = new Review();
        review.setOrderId(req.getOrderId());
        review.setReviewerId(userId);
        review.setRevieweeId(req.getRevieweeId());
        review.setReviewerRole(reviewerIsPublisher ? ReviewerRole.PUBLISHER : ReviewerRole.ACCEPTOR);
        review.setRating(req.getRating());
        review.setContent(req.getContent());
        review.setImageUrls(req.getImageUrls());
        reviewRepository.save(review);
        updateUserRating(req.getRevieweeId(), reviewerIsPublisher, req.getRating());

        notificationService.createNotification(req.getRevieweeId(), NotificationType.REVIEW_RECEIVED,
                "收到新的评价", "你收到了一条新的订单评价。", order.getId());

        if (req.getRating() <= 3) {
            arbitrationService.createTicket(userId, req.getOrderId(), review.getId(), req.getContent(), req.getImageUrls());
        } else {
            quotaService.rewardGoodReview(req.getRevieweeId(), order.getId());
        }
        return ReviewDto.from(review);
    }

    @Override
    public List<ReviewDto> listByUser(Long userId) {
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ReviewDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> listByOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!userId.equals(order.getPublisherId()) && !userId.equals(order.getAcceptorId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return reviewRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(ReviewDto::from)
                .collect(Collectors.toList());
    }

    private void updateUserRating(Long revieweeId, boolean reviewerIsPublisher, int rating) {
        User reviewee = userRepository.findById(revieweeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (reviewerIsPublisher) {
            reviewee.addAcceptorRating(rating);
        } else {
            reviewee.addPublisherRating(rating);
        }
        userRepository.save(reviewee);
    }
}
