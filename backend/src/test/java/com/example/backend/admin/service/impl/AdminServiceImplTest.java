package com.example.backend.admin.service.impl;

import com.example.backend.admin.dto.AdminUserDto;
import com.example.backend.admin.dto.BanUserDto;
import com.example.backend.admin.dto.DashboardDto;
import com.example.backend.admin.entity.AdminOperationLog;
import com.example.backend.admin.repository.AdminOperationLogRepository;
import com.example.backend.common.enums.ArbitrationStatus;
import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.common.enums.RewardType;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.demand.entity.ArbitrationTicket;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.entity.Review;
import com.example.backend.demand.repository.ArbitrationTicketRepository;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.repository.ReviewRepository;
import com.example.backend.notification.repository.NotificationRepository;
import com.example.backend.notification.service.impl.NotificationServiceImpl;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class AdminServiceImplTest {

    @Test
    void shouldBuildDashboardListUsersAndToggleBanStatus() {
        UserRepository userRepository = new UserRepository();
        DemandRepository demandRepository = new DemandRepository();
        OrderRepository orderRepository = new OrderRepository();
        ReviewRepository reviewRepository = new ReviewRepository();
        ArbitrationTicketRepository arbitrationTicketRepository = new ArbitrationTicketRepository();
        NotificationRepository notificationRepository = new NotificationRepository();
        NotificationServiceImpl notificationService = new NotificationServiceImpl(notificationRepository);
        AdminOperationLogRepository adminOperationLogRepository = new AdminOperationLogRepository();
        AdminServiceImpl service = new AdminServiceImpl(userRepository, demandRepository, orderRepository,
                reviewRepository, arbitrationTicketRepository, notificationService, adminOperationLogRepository);

        Demand demand = new Demand();
        demand.setPublisherId(1L);
        demand.setTitle("Open demand");
        demand.setDescription("demand description");
        demand.setCategory(DemandCategory.STUDY);
        demand.setRewardAmount(30);
        demand.setRewardType(RewardType.MONEY);
        demand.setStatus(DemandStatus.OPEN);
        demandRepository.save(demand);

        Order order = new Order();
        order.setDemandId(1L);
        order.setPublisherId(1L);
        order.setAcceptorId(2L);
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setReviewerId(1L);
        review.setRevieweeId(2L);
        review.setRating(4);
        review.setContent("good");
        reviewRepository.save(review);

        ArbitrationTicket ticket = new ArbitrationTicket();
        ticket.setOrderId(order.getId());
        ticket.setReporterId(1L);
        ticket.setReason("dispute");
        ticket.setStatus(ArbitrationStatus.PENDING);
        arbitrationTicketRepository.save(ticket);

        DashboardDto dashboard = service.getDashboard();
        Assertions.assertEquals(2L, dashboard.getTotalUsers());
        Assertions.assertEquals(1L, dashboard.getActiveDemands());
        Assertions.assertEquals(1L, dashboard.getCompletedOrders());
        Assertions.assertEquals(4.0, dashboard.getAverageRating(), 0.0001);
        Assertions.assertEquals(1L, dashboard.getPendingArbitrations());

        List<AdminUserDto> users = service.listUsers("alice", null);
        Assertions.assertEquals(1, users.size());
        Assertions.assertEquals("alice", users.get(0).getUsername());

        User bob = new User();
        bob.setUsername("bob");
        bob.setPasswordHash("hash");
        bob.setStudentId("2024001999");
        bob.setRealName("Bob");
        bob.setCreditScore(90);
        bob.setRole(UserRole.STUDENT);
        bob.setStatus(UserStatus.ACTIVE);
        userRepository.save(bob);

        BanUserDto banUserDto = new BanUserDto();
        banUserDto.setReason("rule violation");
        service.banUser(bob.getId(), 2L, banUserDto);
        Assertions.assertEquals(UserStatus.BANNED, userRepository.findById(bob.getId()).orElseThrow(AssertionError::new).getStatus());
        Assertions.assertEquals(1, notificationRepository.findByUserIdOrderByCreatedAtDesc(bob.getId()).size());

        service.unbanUser(bob.getId(), 2L);
        Assertions.assertEquals(UserStatus.ACTIVE, userRepository.findById(bob.getId()).orElseThrow(AssertionError::new).getStatus());
        Assertions.assertEquals(2, notificationRepository.findByUserIdOrderByCreatedAtDesc(bob.getId()).size());

        List<AdminOperationLog> logs = adminOperationLogRepository.findAll();
        Assertions.assertEquals(2, logs.size());
    }
}
