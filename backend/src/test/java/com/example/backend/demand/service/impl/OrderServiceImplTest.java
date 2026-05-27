package com.example.backend.demand.service.impl;

import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.service.ConversationService;
import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.common.enums.RewardType;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.demand.dto.AcceptOrderDto;
import com.example.backend.demand.dto.DemandDto;
import com.example.backend.demand.dto.DemandSearchCriteria;
import com.example.backend.demand.dto.DemandUpdateDto;
import com.example.backend.demand.dto.OrderDto;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.pattern.observer.OrderEventPublisher;
import com.example.backend.demand.pattern.strategy.OrderStateContext;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.repository.OrderStatusLogRepository;
import com.example.backend.demand.service.DemandService;
import com.example.backend.user.dto.CreditRecordDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CreditService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class OrderServiceImplTest {

    @Test
    void shouldAcceptDemandAndApplyCancelPenaltyRule() {
        UserRepository userRepository = new UserRepository();
        User publisher = new User();
        publisher.setUsername("publisher");
        publisher.setPasswordHash("x");
        publisher.setStudentId("s1");
        publisher.setRealName("p");
        publisher.setStatus(UserStatus.ACTIVE);
        userRepository.save(publisher);

        User acceptor = new User();
        acceptor.setUsername("acceptor");
        acceptor.setPasswordHash("x");
        acceptor.setStudentId("s2");
        acceptor.setRealName("a");
        acceptor.setStatus(UserStatus.ACTIVE);
        userRepository.save(acceptor);

        DemandDto demand = new DemandDto();
        demand.setId(100L);
        demand.setPublisherId(publisher.getId());
        demand.setTitle("Need help");
        demand.setDescription("desc");
        demand.setCategory(DemandCategory.ERRAND);
        demand.setRewardAmount(20);
        demand.setRewardType(RewardType.MONEY);
        demand.setStatus(DemandStatus.OPEN);

        DemandService demandService = new InMemoryDemandService(demand);
        OrderRepository orderRepository = new OrderRepository();
        OrderStatusLogRepository logRepository = new OrderStatusLogRepository();
        OrderStateContext stateContext = new OrderStateContext();
        OrderEventPublisher eventPublisher = new OrderEventPublisher(new NoopEventPublisher());
        CreditService creditService = new InMemoryCreditService();
        ConversationService conversationService = new NoopConversationService();

        OrderServiceImpl service = new OrderServiceImpl(
                orderRepository,
                logRepository,
                stateContext,
                eventPublisher,
                demandService,
                userRepository,
                creditService,
                conversationService);

        AcceptOrderDto request = new AcceptOrderDto();
        request.setDemandId(demand.getId());
        OrderDto accepted = service.acceptDemand(acceptor.getId(), request);
        Assertions.assertEquals(OrderStatus.ACCEPTED, accepted.getStatus());

        Order stored = orderRepository.findById(accepted.getId()).orElseThrow(AssertionError::new);
        stored.setAcceptedAt(LocalDateTime.now().minusMinutes(11));
        orderRepository.save(stored);

        OrderDto cancelled = service.cancel(stored.getId(), acceptor.getId(), null);
        Assertions.assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        Assertions.assertEquals(-10, cancelled.getCreditPenalty());
    }

    private static final class InMemoryDemandService implements DemandService {
        private final DemandDto demand;

        private InMemoryDemandService(DemandDto demand) {
            this.demand = demand;
        }

        @Override
        public DemandDto createDemand(Long userId, com.example.backend.demand.dto.DemandCreateDto req) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DemandDto> search(DemandSearchCriteria criteria) {
            return List.of(demand);
        }

        @Override
        public DemandDto getDetail(Long demandId) {
            return demand.getId().equals(demandId) ? demand : null;
        }

        @Override
        public void updateStatus(Long demandId, DemandStatus status) {
            if (demand.getId().equals(demandId)) {
                demand.setStatus(status);
            }
        }

        @Override
        public DemandDto update(Long demandId, Long userId, DemandUpdateDto req) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cancel(Long demandId, Long userId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class InMemoryCreditService implements CreditService {
        private final List<CreditRecordDto> records = new ArrayList<CreditRecordDto>();

        @Override
        public boolean checkCredit(Long userId) {
            return true;
        }

        @Override
        public int getCreditScore(Long userId) {
            return 100;
        }

        @Override
        public CreditRecordDto addCredit(Long userId, int amount, String reason, Long relatedOrderId) {
            CreditRecordDto dto = new CreditRecordDto();
            dto.setUserId(userId);
            dto.setChangeAmount(amount);
            dto.setReason(reason);
            dto.setRelatedOrderId(relatedOrderId);
            records.add(dto);
            return dto;
        }

        @Override
        public CreditRecordDto deductCredit(Long userId, int amount, String reason, Long relatedOrderId) {
            CreditRecordDto dto = new CreditRecordDto();
            dto.setUserId(userId);
            dto.setChangeAmount(-amount);
            dto.setReason(reason);
            dto.setRelatedOrderId(relatedOrderId);
            records.add(dto);
            return dto;
        }

        @Override
        public List<CreditRecordDto> listRecords(Long userId) {
            return records;
        }
    }

    private static final class NoopConversationService implements ConversationService {
        @Override
        public com.example.backend.chat.dto.ConversationDto getOrCreate(Long userId, CreateConversationDto req) {
            return new com.example.backend.chat.dto.ConversationDto();
        }

        @Override
        public List<com.example.backend.chat.dto.ConversationDto> listConversations(Long userId) {
            return List.of();
        }

        @Override
        public com.example.backend.chat.dto.MessageDto sendMessage(Long conversationId, Long senderId, com.example.backend.chat.dto.SendMessageDto req) {
            return new com.example.backend.chat.dto.MessageDto();
        }

        @Override
        public List<com.example.backend.chat.dto.MessageDto> getMessages(Long conversationId, Long userId, int page, int size) {
            return List.of();
        }

        @Override
        public void markRead(Long conversationId, Long userId) {
        }
    }

    private static final class NoopEventPublisher implements ApplicationEventPublisher {
        @Override
        public void publishEvent(Object event) {
        }
    }
}
