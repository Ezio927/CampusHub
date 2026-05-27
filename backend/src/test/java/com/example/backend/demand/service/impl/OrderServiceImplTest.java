package com.example.backend.demand.service.impl;

import com.example.backend.chat.repository.ConversationRepository;
import com.example.backend.chat.repository.MessageRepository;
import com.example.backend.chat.service.impl.ConversationServiceImpl;
import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.common.enums.RewardType;
import com.example.backend.demand.dto.AcceptOrderDto;
import com.example.backend.demand.dto.OrderDto;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.entity.OrderStatusLog;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.repository.OrderStatusLogRepository;
import com.example.backend.notification.repository.NotificationRepository;
import com.example.backend.notification.service.impl.NotificationServiceImpl;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.CreditRecordRepository;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CreditService;
import com.example.backend.user.service.impl.CreditServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class OrderServiceImplTest {

    @Test
    void shouldAcceptDemandAndCreateConversationAndNotification() {
        UserRepository userRepository = new UserRepository();
        DemandRepository demandRepository = new DemandRepository();
        OrderRepository orderRepository = new OrderRepository();
        OrderStatusLogRepository orderStatusLogRepository = new OrderStatusLogRepository();
        ConversationRepository conversationRepository = new ConversationRepository();
        MessageRepository messageRepository = new MessageRepository();
        NotificationRepository notificationRepository = new NotificationRepository();
        NotificationServiceImpl notificationService = new NotificationServiceImpl(notificationRepository);
        CreditRecordRepository creditRecordRepository = new CreditRecordRepository();
        CreditService creditService = new CreditServiceImpl(userRepository, creditRecordRepository, notificationService);
        ConversationServiceImpl conversationService = new ConversationServiceImpl(
                conversationRepository, messageRepository, userRepository, demandRepository, notificationService);
        OrderServiceImpl service = new OrderServiceImpl(orderRepository, orderStatusLogRepository, demandRepository,
                userRepository, creditService, conversationService, notificationService);

        Demand demand = new Demand();
        demand.setPublisherId(1L);
        demand.setTitle("Need help");
        demand.setDescription("please help");
        demand.setCategory(DemandCategory.ERRAND);
        demand.setRewardAmount(20);
        demand.setRewardType(RewardType.MONEY);
        demand.setStatus(DemandStatus.OPEN);
        demandRepository.save(demand);

        AcceptOrderDto request = new AcceptOrderDto();
        request.setDemandId(demand.getId());

        OrderDto orderDto = service.acceptDemand(2L, request);
        Assertions.assertEquals(OrderStatus.ACCEPTED, orderDto.getStatus());
        Assertions.assertEquals(1L, orderDto.getPublisherId());
        Assertions.assertEquals(2L, orderDto.getAcceptorId());

        Assertions.assertTrue(orderRepository.findByDemandId(demand.getId()).isPresent());
        Assertions.assertEquals(1, orderStatusLogRepository.findByOrderIdOrderByCreatedAtDesc(orderDto.getId()).size());

        Assertions.assertEquals(1, notificationRepository.findByUserIdOrderByCreatedAtDesc(1L).size());
        Assertions.assertEquals(1, conversationRepository.findByUserIdOrderByLastMessageAtDesc(1L).size());

        User acceptor = userRepository.findById(2L).orElseThrow(AssertionError::new);
        Assertions.assertTrue(creditService.checkCredit(acceptor.getId()));
    }
}
