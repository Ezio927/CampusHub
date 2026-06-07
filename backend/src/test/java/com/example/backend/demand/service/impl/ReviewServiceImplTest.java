package com.example.backend.demand.service.impl;

import com.example.backend.common.BusinessException;
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
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.QuotaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ArbitrationService arbitrationService;
    @Mock private NotificationService notificationService;
    @Mock private QuotaService quotaService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void shouldSubmitReviewForCompletedOrder() {
        Order order = new Order();
        order.setId(10L);
        order.setPublisherId(1L);
        order.setAcceptorId(2L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(reviewRepository.findByOrderIdAndReviewerId(10L, 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(createUser(2L)));

        ReviewSubmitDto req = new ReviewSubmitDto();
        req.setOrderId(10L);
        req.setRevieweeId(2L);
        req.setRating(5);

        ReviewDto result = reviewService.submitReview(1L, req);
        assertNotNull(result);
        verify(reviewRepository).save(any(Review.class));
        verify(notificationService).createNotification(eq(2L), eq(NotificationType.REVIEW_RECEIVED), anyString(), anyString(), eq(10L));
    }

    @Test
    void shouldRejectDuplicateReview() {
        Order order = new Order();
        order.setId(10L);
        order.setPublisherId(1L);
        order.setAcceptorId(2L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(reviewRepository.findByOrderIdAndReviewerId(10L, 1L)).thenReturn(Optional.of(new Review()));

        ReviewSubmitDto req = new ReviewSubmitDto();
        req.setOrderId(10L);
        req.setRevieweeId(2L);
        req.setRating(4);

        assertThrows(BusinessException.class, () -> reviewService.submitReview(1L, req));
    }

    @Test
    void shouldRejectReviewForNonParticipant() {
        Order order = new Order();
        order.setId(10L);
        order.setPublisherId(1L);
        order.setAcceptorId(2L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        ReviewSubmitDto req = new ReviewSubmitDto();
        req.setOrderId(10L);
        req.setRevieweeId(1L);
        req.setRating(3);

        assertThrows(BusinessException.class, () -> reviewService.submitReview(99L, req));
    }

    @Test
    void shouldRejectReviewForWrongReviewee() {
        Order order = new Order();
        order.setId(10L);
        order.setPublisherId(1L);
        order.setAcceptorId(2L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        ReviewSubmitDto req = new ReviewSubmitDto();
        req.setOrderId(10L);
        req.setRevieweeId(99L);
        req.setRating(3);

        assertThrows(BusinessException.class, () -> reviewService.submitReview(1L, req));
    }

    @Test
    void shouldRejectInvalidRating() {
        ReviewSubmitDto req = new ReviewSubmitDto();
        req.setOrderId(10L);
        req.setRevieweeId(2L);
        req.setRating(0);
        assertThrows(BusinessException.class, () -> reviewService.submitReview(1L, req));

        req.setRating(6);
        assertThrows(BusinessException.class, () -> reviewService.submitReview(1L, req));
    }

    @Test
    void shouldListReviewsByUser() {
        Review r1 = new Review();
        r1.setId(1L);
        r1.setReviewerId(1L);
        r1.setRevieweeId(2L);
        r1.setReviewerRole(ReviewerRole.PUBLISHER);
        r1.setRating(4);
        when(reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(r1));

        List<ReviewDto> result = reviewService.listByUser(2L);
        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getRating());
    }

    private User createUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user" + id);
        return u;
    }
}
