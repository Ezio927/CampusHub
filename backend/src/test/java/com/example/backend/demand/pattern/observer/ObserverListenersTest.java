package com.example.backend.demand.pattern.observer;

import com.example.backend.common.enums.*;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.demand.service.DemandService;
import com.example.backend.demand.service.impl.DemandServiceImpl;
import com.example.backend.notification.entity.Notification;
import com.example.backend.notification.repository.NotificationRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.CreditRecordRepository;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CreditService;
import com.example.backend.user.service.impl.CreditServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObserverListenersTest {

    private UserRepository userRepository;
    private NotificationRepository notificationRepository;
    private CreditService creditService;
    private CreditScoreListener creditScoreListener;
    private NotificationListener notificationListener;
    private DemandStatusListener demandStatusListener;
    private DemandRepository demandRepository;
    private DemandService demandService;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        notificationRepository = new NotificationRepository();

        NotificationServiceImplForTest notificationService = new NotificationServiceImplForTest(notificationRepository);
        CreditRecordRepository creditRecordRepository = new CreditRecordRepository();
        creditService = new CreditServiceImpl(userRepository, creditRecordRepository, notificationService);

        creditScoreListener = new CreditScoreListener(creditService);
        notificationListener = new NotificationListener(notificationService);
        demandRepository = new DemandRepository();
        demandService = new DemandServiceImplForTest(demandRepository);
        demandStatusListener = new DemandStatusListener(demandService);

        User alice = userRepository.findById(1L).orElseThrow();
        alice.setStatus(UserStatus.ACTIVE);
        userRepository.save(alice);
        User admin = userRepository.findById(2L).orElseThrow();
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
    }

    @Test
    void creditScoreListenerShouldAddCreditOnCompletion() {
        Order order = buildOrder(OrderStatus.COMPLETED);
        int before = userRepository.findById(2L).orElseThrow().getCreditScore();

        creditScoreListener.onOrderStatusChanged(buildEvent(order, OrderStatus.PENDING_CONFIRM));

        int after = userRepository.findById(2L).orElseThrow().getCreditScore();
        Assertions.assertEquals(before + 5, after);
    }

    @Test
    void creditScoreListenerShouldNotAddCreditForNonCompleted() {
        Order order = buildOrder(OrderStatus.ACCEPTED);
        int before = userRepository.findById(2L).orElseThrow().getCreditScore();

        creditScoreListener.onOrderStatusChanged(buildEvent(order, OrderStatus.PENDING_ACCEPT));

        int after = userRepository.findById(2L).orElseThrow().getCreditScore();
        Assertions.assertEquals(before, after);
    }

    @Test
    void notificationListenerShouldNotifyOnAccepted() {
        Order order = buildOrder(OrderStatus.ACCEPTED);
        notificationListener.onOrderStatusChanged(buildEvent(order, OrderStatus.PENDING_ACCEPT));

        Assertions.assertTrue(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L).stream()
                .anyMatch(n -> n.getType() == NotificationType.ORDER_ACCEPTED));
    }

    @Test
    void notificationListenerShouldNotifyOnCompleted() {
        Order order = buildOrder(OrderStatus.COMPLETED);
        notificationListener.onOrderStatusChanged(buildEvent(order, OrderStatus.PENDING_CONFIRM));

        Assertions.assertTrue(notificationRepository.findByUserIdOrderByCreatedAtDesc(2L).stream()
                .anyMatch(n -> n.getType() == NotificationType.ORDER_COMPLETED));
    }

    @Test
    void notificationListenerShouldNotifyOnCancelled() {
        Order order = buildOrder(OrderStatus.CANCELLED);
        notificationListener.onOrderStatusChanged(buildEvent(order, OrderStatus.ACCEPTED));

        Assertions.assertTrue(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L).stream()
                .anyMatch(n -> n.getType() == NotificationType.ORDER_CANCELLED));
    }

    @Test
    void demandStatusListenerShouldSyncDemandStatus() {
        Demand demand = new Demand();
        demand.setId(10L);
        demand.setPublisherId(1L);
        demand.setTitle("test demand");
        demand.setCategory(DemandCategory.ERRAND);
        demand.setStatus(DemandStatus.OPEN);
        demandRepository.save(demand);

        Order order = new Order();
        order.setDemandId(10L);
        order.setPublisherId(1L);
        order.setAcceptorId(2L);
        order.setStatus(OrderStatus.COMPLETED);

        demandStatusListener.onOrderStatusChanged(buildEvent(order, OrderStatus.PENDING_CONFIRM));

        Demand updated = demandRepository.findById(10L).orElseThrow();
        Assertions.assertEquals(DemandStatus.COMPLETED, updated.getStatus());
    }

    private Order buildOrder(OrderStatus status) {
        Order order = new Order();
        order.setId(1L);
        order.setDemandId(10L);
        order.setPublisherId(1L);
        order.setAcceptorId(2L);
        order.setStatus(status);
        return order;
    }

    private OrderStatusChangedEvent buildEvent(Order order, OrderStatus fromStatus) {
        return new OrderStatusChangedEvent(
                order.getId(), order.getDemandId(),
                order.getPublisherId(), order.getAcceptorId(),
                fromStatus, order.getStatus(), order.getAcceptorId(), "test remark"
        );
    }

    /** Simple notification service that doesn't need full NotificationServiceImpl */
    private static class NotificationServiceImplForTest extends com.example.backend.notification.service.impl.NotificationServiceImpl {
        public NotificationServiceImplForTest(NotificationRepository repo) {
            super(repo);
        }
    }

    /** Simple demand service adapter for listener testing */
    private static class DemandServiceImplForTest extends DemandServiceImpl {
        private final DemandRepository demandRepository;

        public DemandServiceImplForTest(DemandRepository demandRepository) {
            super(null, demandRepository, null, null, null);
            this.demandRepository = demandRepository;
        }

        @Override
        public void updateStatus(Long demandId, com.example.backend.common.enums.DemandStatus status) {
            demandRepository.findById(demandId).ifPresent(d -> {
                d.setStatus(status);
                demandRepository.save(d);
            });
        }
    }
}
