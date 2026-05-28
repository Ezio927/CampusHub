package com.example.backend.boundary;

import com.example.backend.admin.controller.AdminController;
import com.example.backend.admin.dto.AdminUserDto;
import com.example.backend.admin.dto.BanUserDto;
import com.example.backend.admin.dto.DashboardDto;
import com.example.backend.admin.service.AdminService;
import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.RegisterRequest;
import com.example.backend.auth.security.AuthContext;
import com.example.backend.auth.security.JwtTokenProvider;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.CertStatus;
import com.example.backend.common.enums.CertType;
import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.MessageType;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.common.enums.RewardType;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.config.JwtConfig;
import com.example.backend.common.GlobalExceptionHandler;
import com.example.backend.demand.dto.AcceptOrderDto;
import com.example.backend.demand.dto.ArbitrationCreateDto;
import com.example.backend.demand.dto.ArbitrationResolveDto;
import com.example.backend.demand.dto.DemandCreateDto;
import com.example.backend.demand.dto.OrderDto;
import com.example.backend.demand.dto.ReviewSubmitDto;
import com.example.backend.demand.entity.ArbitrationTicket;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.entity.Order;
import com.example.backend.demand.repository.ArbitrationTicketRepository;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.demand.repository.OrderRepository;
import com.example.backend.demand.repository.OrderStatusLogRepository;
import com.example.backend.demand.repository.ReviewRepository;
import com.example.backend.demand.service.impl.ArbitrationServiceImpl;
import com.example.backend.demand.service.impl.OrderServiceImpl;
import com.example.backend.demand.service.impl.ReviewServiceImpl;
import com.example.backend.demand.pattern.factory.DemandFactory;
import com.example.backend.demand.pattern.factory.ErrandValidator;
import com.example.backend.demand.pattern.factory.SecondhandValidator;
import com.example.backend.demand.pattern.factory.SkillValidator;
import com.example.backend.demand.pattern.factory.StudyValidator;
import com.example.backend.chat.dto.CreateConversationDto;
import com.example.backend.chat.dto.SendMessageDto;
import com.example.backend.chat.repository.ConversationRepository;
import com.example.backend.chat.repository.MessageRepository;
import com.example.backend.chat.service.impl.ConversationServiceImpl;
import com.example.backend.notification.repository.NotificationRepository;
import com.example.backend.notification.service.impl.NotificationServiceImpl;
import com.example.backend.user.dto.CertificationSubmitDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.CertificationRepository;
import com.example.backend.user.repository.CreditRecordRepository;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CreditService;
import com.example.backend.user.service.impl.CertificationServiceImpl;
import com.example.backend.user.service.impl.CreditServiceImpl;
import com.example.backend.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

class MemberBBoundaryTest {

    @Test
    void shouldRejectDuplicateAndBlankAuthInputs() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret");
        jwtConfig.setExpirationMinutes(60);
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtConfig);
        com.example.backend.auth.service.impl.AuthServiceImpl authService =
                new com.example.backend.auth.service.impl.AuthServiceImpl(userRepository, userService, jwtTokenProvider);

        RegisterRequest blank = new RegisterRequest();
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.register(blank)).getCode());

        RegisterRequest duplicateUsername = new RegisterRequest();
        duplicateUsername.setUsername("alice");
        duplicateUsername.setPassword("pwd12345");
        duplicateUsername.setStudentId("2024999001");
        duplicateUsername.setRealName("Alice Two");
        Assertions.assertEquals(ErrorCode.USERNAME_EXISTS.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.register(duplicateUsername)).getCode());

        RegisterRequest duplicateStudentId = new RegisterRequest();
        duplicateStudentId.setUsername("newuser");
        duplicateStudentId.setPassword("pwd12345");
        duplicateStudentId.setStudentId("2024001001");
        duplicateStudentId.setRealName("Alice Three");
        Assertions.assertEquals(ErrorCode.STUDENT_ID_EXISTS.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.register(duplicateStudentId)).getCode());

        LoginRequest blankLogin = new LoginRequest();
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.login(blankLogin)).getCode());

        LoginRequest wrongPassword = new LoginRequest();
        wrongPassword.setUsername("alice");
        wrongPassword.setPassword("wrong-password");
        Assertions.assertEquals(ErrorCode.UNAUTHORIZED.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.login(wrongPassword)).getCode());
    }

    @Test
    void shouldRejectCertificationBoundaryCases() {
        UserRepository userRepository = new UserRepository();
        CertificationRepository certificationRepository = new CertificationRepository();
        NotificationRepository notificationRepository = new NotificationRepository();
        NotificationServiceImpl notificationService = new NotificationServiceImpl(notificationRepository);
        CertificationServiceImpl certificationService = new CertificationServiceImpl(
                certificationRepository, userRepository, notificationService);

        User banned = new User();
        banned.setUsername("banned-user");
        banned.setPasswordHash("hash");
        banned.setStudentId("BANNED001");
        banned.setRealName("Banned");
        banned.setCreditScore(100);
        banned.setRole(UserRole.STUDENT);
        banned.setStatus(UserStatus.BANNED);
        userRepository.save(banned);

        CertificationSubmitDto blank = new CertificationSubmitDto();
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> certificationService.submit(1L, blank)).getCode());

        CertificationSubmitDto missingImage = new CertificationSubmitDto();
        missingImage.setCertType(CertType.STUDENT_CARD);
        missingImage.setStudentId("2024001001");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> certificationService.submit(1L, missingImage)).getCode());

        CertificationSubmitDto bannedReq = new CertificationSubmitDto();
        bannedReq.setCertType(CertType.STUDENT_ID);
        bannedReq.setStudentId("BAN-001");
        Assertions.assertEquals(ErrorCode.ACCOUNT_BANNED.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> certificationService.submit(banned.getId(), bannedReq)).getCode());

        CertificationSubmitDto submit = new CertificationSubmitDto();
        submit.setCertType(CertType.STUDENT_ID);
        submit.setStudentId("2024001888");
        CertificationDtoOrThrow certificationDtoOrThrow = new CertificationDtoOrThrow(certificationService.submit(1L, submit));
        Assertions.assertEquals(CertStatus.PENDING, certificationRepository.findByUserId(1L).orElseThrow(AssertionError::new).getStatus());

        Assertions.assertEquals(ErrorCode.CERTIFICATION_PENDING_EXISTS.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> certificationService.submit(1L, submit)).getCode());

        User alice = userRepository.findById(1L).orElseThrow(AssertionError::new);
        alice.setStatus(UserStatus.ACTIVE);
        userRepository.save(alice);
        Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> certificationService.review(certificationDtoOrThrow.getId(), true, null, 1L)).getCode());

        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> certificationService.review(certificationDtoOrThrow.getId(), false, "", 2L)).getCode());

        certificationService.review(certificationDtoOrThrow.getId(), true, null, 2L);
        Assertions.assertEquals(ErrorCode.CERTIFICATION_ALREADY_APPROVED.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> certificationService.submit(1L, submit)).getCode());
    }

    @Test
    void shouldRejectDemandAndOrderBoundaryCases() {
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
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderStatusLogRepository, demandRepository,
                userRepository, creditService, conversationService, notificationService);
        DemandFactory demandFactory = new DemandFactory(new ErrandValidator(), new SkillValidator(),
                new StudyValidator(), new SecondhandValidator());
        com.example.backend.demand.service.impl.DemandServiceImpl demandService =
                new com.example.backend.demand.service.impl.DemandServiceImpl(
                        demandFactory, demandRepository, new com.example.backend.demand.repository.DemandImageRepository(),
                        new com.example.backend.demand.repository.TagRepository(), userRepository);

        User admin = userRepository.findById(2L).orElseThrow(AssertionError::new);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
        User alice = userRepository.findById(1L).orElseThrow(AssertionError::new);
        alice.setStatus(UserStatus.ACTIVE);
        alice.setCreditScore(50);
        userRepository.save(alice);

        DemandCreateDto invalidCategory = new DemandCreateDto();
        invalidCategory.setTitle("t");
        invalidCategory.setDescription("d");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> demandService.createDemand(2L, invalidCategory)).getCode());

        DemandCreateDto errandMissingLocation = buildDemand("errand", DemandCategory.ERRAND, RewardType.MONEY);
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> demandService.createDemand(2L, errandMissingLocation)).getCode());

        DemandCreateDto skillMissingTags = buildDemand("skill", DemandCategory.SKILL, RewardType.MONEY);
        skillMissingTags.setLocationTag("library");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> demandService.createDemand(2L, skillMissingTags)).getCode());

        DemandCreateDto studyMissingDeadline = buildDemand("study", DemandCategory.STUDY, RewardType.MONEY);
        studyMissingDeadline.setLocationTag("library");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> demandService.createDemand(2L, studyMissingDeadline)).getCode());

        DemandCreateDto secondhandMissingImages = buildDemand("secondhand", DemandCategory.SECONDHAND, RewardType.MONEY);
        secondhandMissingImages.setLocationTag("library");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> demandService.createDemand(2L, secondhandMissingImages)).getCode());

        DemandCreateDto validDemandReq = buildDemand("open demand", DemandCategory.ERRAND, RewardType.MONEY);
        validDemandReq.setLocationTag("campus gate");
        DemandDtoOrThrow demandDtoOrThrow = new DemandDtoOrThrow(demandService.createDemand(2L, validDemandReq));

        DemandUpdateDtoHolder updateByNonOwner = new DemandUpdateDtoHolder();
        updateByNonOwner.setTitle("changed");
        Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> demandService.update(demandDtoOrThrow.getId(), 1L, updateByNonOwner.value)).getCode());

        Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> demandService.cancel(demandDtoOrThrow.getId(), 1L)).getCode());

        AcceptOrderDto accept = new AcceptOrderDto();
        accept.setDemandId(demandDtoOrThrow.getId());
        Assertions.assertEquals(ErrorCode.CREDIT_INSUFFICIENT.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> orderService.acceptDemand(1L, accept)).getCode());

        alice.setCreditScore(100);
        userRepository.save(alice);
        OrderDto acceptedOrder = orderService.acceptDemand(1L, accept);
        Assertions.assertEquals(OrderStatus.ACCEPTED, acceptedOrder.getStatus());
        Assertions.assertEquals(ErrorCode.DEMAND_ALREADY_ACCEPTED.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> orderService.acceptDemand(1L, accept)).getCode());

        User third = new User();
        third.setUsername("charlie-order");
        third.setPasswordHash("hash");
        third.setStudentId("2024003333");
        third.setRealName("Charlie Order");
        third.setCreditScore(100);
        third.setRole(UserRole.STUDENT);
        third.setStatus(UserStatus.ACTIVE);
        userRepository.save(third);
        Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> orderService.getDetail(acceptedOrder.getId(), third.getId())).getCode());

        Assertions.assertEquals(ErrorCode.CONFLICT.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> orderService.confirm(acceptedOrder.getId(), 1L)).getCode());

        Demand demand = demandRepository.findById(demandDtoOrThrow.getId()).orElseThrow(AssertionError::new);
        demand.markAsAccepted();
        demandRepository.save(demand);
        Assertions.assertEquals(ErrorCode.CONFLICT.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> demandService.update(demand.getId(), 2L, updateByNonOwner.value)).getCode());
        Assertions.assertEquals(ErrorCode.CONFLICT.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> demandService.cancel(demand.getId(), 2L)).getCode());
    }

    @Test
    void shouldRejectReviewAndArbitrationBoundaryCases() {
        UserRepository userRepository = new UserRepository();
        DemandRepository demandRepository = new DemandRepository();
        OrderRepository orderRepository = new OrderRepository();
        ReviewRepository reviewRepository = new ReviewRepository();
        ArbitrationTicketRepository arbitrationTicketRepository = new ArbitrationTicketRepository();
        NotificationRepository notificationRepository = new NotificationRepository();
        NotificationServiceImpl notificationService = new NotificationServiceImpl(notificationRepository);
        ArbitrationServiceImpl arbitrationService = new ArbitrationServiceImpl(
                arbitrationTicketRepository, userRepository, notificationService);
        ReviewServiceImpl reviewService = new ReviewServiceImpl(reviewRepository, orderRepository, arbitrationService, notificationService);

        User admin = userRepository.findById(2L).orElseThrow(AssertionError::new);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
        User alice = userRepository.findById(1L).orElseThrow(AssertionError::new);
        alice.setStatus(UserStatus.ACTIVE);
        userRepository.save(alice);

        Order pendingOrder = new Order();
        pendingOrder.setDemandId(10L);
        pendingOrder.setPublisherId(1L);
        pendingOrder.setAcceptorId(2L);
        pendingOrder.setStatus(OrderStatus.ACCEPTED);
        orderRepository.save(pendingOrder);

        ReviewSubmitDto emptyReview = new ReviewSubmitDto();
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> reviewService.submitReview(1L, emptyReview)).getCode());

        ReviewSubmitDto badRating = new ReviewSubmitDto();
        badRating.setOrderId(pendingOrder.getId());
        badRating.setRevieweeId(2L);
        badRating.setRating(0);
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> reviewService.submitReview(1L, badRating)).getCode());

        ReviewSubmitDto notFinished = new ReviewSubmitDto();
        notFinished.setOrderId(pendingOrder.getId());
        notFinished.setRevieweeId(2L);
        notFinished.setRating(5);
        Assertions.assertEquals(ErrorCode.CONFLICT.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> reviewService.submitReview(1L, notFinished)).getCode());

        pendingOrder.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(pendingOrder);
        ReviewSubmitDto goodReview = new ReviewSubmitDto();
        goodReview.setOrderId(pendingOrder.getId());
        goodReview.setRevieweeId(2L);
        goodReview.setRating(5);
        goodReview.setContent("nice");
        Assertions.assertNotNull(reviewService.submitReview(1L, goodReview));
        Assertions.assertEquals(ErrorCode.REVIEW_DUPLICATED.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> reviewService.submitReview(1L, goodReview)).getCode());

        Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> reviewService.listByOrder(pendingOrder.getId(), 999L)).getCode());

        ArbitrationCreateDto emptyArbitration = new ArbitrationCreateDto();
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> arbitrationService.createTicket(1L, emptyArbitration)).getCode());

        ArbitrationDtoHolder ticket = new ArbitrationDtoHolder(arbitrationService.createTicket(1L, pendingOrder.getId(), 1L, "reason", Collections.singletonList("e1")));
        Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> arbitrationService.resolve(ticket.getId(), 1L, buildResolve("RESOLVE", "done"))).getCode());

        Assertions.assertNotNull(arbitrationService.resolve(ticket.getId(), 2L, buildResolve("RESOLVE", "done")));
        Assertions.assertEquals(ErrorCode.CONFLICT.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> arbitrationService.resolve(ticket.getId(), 2L, buildResolve("RESOLVE", "done"))).getCode());
    }

    @Test
    void shouldRejectChatAndNotificationBoundaryCases() {
        UserRepository userRepository = new UserRepository();
        DemandRepository demandRepository = new DemandRepository();
        ConversationRepository conversationRepository = new ConversationRepository();
        MessageRepository messageRepository = new MessageRepository();
        NotificationRepository notificationRepository = new NotificationRepository();
        NotificationServiceImpl notificationService = new NotificationServiceImpl(notificationRepository);
        ConversationServiceImpl conversationService = new ConversationServiceImpl(
                conversationRepository, messageRepository, userRepository, demandRepository, notificationService);

        User admin = userRepository.findById(2L).orElseThrow(AssertionError::new);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
        User alice = userRepository.findById(1L).orElseThrow(AssertionError::new);
        alice.setStatus(UserStatus.ACTIVE);
        userRepository.save(alice);
        User third = new User();
        third.setUsername("charlie");
        third.setPasswordHash("hash");
        third.setStudentId("2024003000");
        third.setRealName("Charlie");
        third.setCreditScore(100);
        third.setRole(UserRole.STUDENT);
        third.setStatus(UserStatus.ACTIVE);
        userRepository.save(third);

        CreateConversationDto emptyConversation = new CreateConversationDto();
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> conversationService.getOrCreate(1L, emptyConversation)).getCode());

        CreateConversationDto createConversationDto = new CreateConversationDto();
        createConversationDto.setTargetUserId(2L);
        createConversationDto.setDemandId(99L);
        com.example.backend.chat.dto.ConversationDto conversationDto = conversationService.getOrCreate(1L, createConversationDto);
        Assertions.assertNotNull(conversationDto.getId());

        com.example.backend.chat.dto.SendMessageDto emptyMessage = new com.example.backend.chat.dto.SendMessageDto();
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> conversationService.sendMessage(conversationDto.getId(), 1L, emptyMessage)).getCode());

        com.example.backend.chat.dto.SendMessageDto validMessage = new com.example.backend.chat.dto.SendMessageDto();
        validMessage.setContent("hello");
        validMessage.setMessageType(MessageType.TEXT);
        Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> conversationService.sendMessage(conversationDto.getId(), third.getId(), validMessage)).getCode());
        Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> conversationService.markRead(conversationDto.getId(), third.getId())).getCode());

        NotificationRepository localNotificationRepository = new NotificationRepository();
        NotificationServiceImpl localNotificationService = new NotificationServiceImpl(localNotificationRepository);
        Assertions.assertEquals(ErrorCode.NOT_FOUND.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> localNotificationService.markRead(999L, 1L)).getCode());
    }

    @Test
    void shouldRejectNonAdminControllerAccess() {
        AdminController controller = new AdminController(new AdminService() {
            @Override
            public DashboardDto getDashboard() {
                return new DashboardDto();
            }

            @Override
            public java.util.List<AdminUserDto> listUsers(String keyword, String status) {
                return Collections.emptyList();
            }

            @Override
            public void banUser(Long userId, Long adminId, BanUserDto req) {
            }

            @Override
            public void unbanUser(Long userId, Long adminId) {
            }

            @Override
            public com.example.backend.user.dto.CertificationDto reviewCertification(
                    Long certificationId,
                    Long adminId,
                    com.example.backend.user.dto.CertificationReviewDto req) {
                return null;
            }

            @Override
            public com.example.backend.demand.dto.ArbitrationDto resolveArbitration(
                    Long ticketId,
                    Long adminId,
                    ArbitrationResolveDto req) {
                return null;
            }
        });

        AuthContext.setCurrentUser(new AuthContext.AuthUser(1L, "alice", UserRole.STUDENT));
        try {
            Assertions.assertEquals(ErrorCode.FORBIDDEN.getCode(),
                    Assertions.assertThrows(BusinessException.class, controller::dashboard).getCode());
        } finally {
            AuthContext.clear();
        }
    }

    @Test
    void shouldRejectIllegalEnumAtControllerBoundary() throws Exception {
        com.example.backend.demand.controller.DemandController controller =
                new com.example.backend.demand.controller.DemandController(new com.example.backend.demand.service.DemandService() {
                    @Override
                    public com.example.backend.demand.dto.DemandDto createDemand(Long userId, DemandCreateDto req) {
                        throw new AssertionError("service should not be reached");
                    }

                    @Override
                    public java.util.List<com.example.backend.demand.dto.DemandDto> search(com.example.backend.demand.dto.DemandSearchCriteria criteria) {
                        return Collections.emptyList();
                    }

                    @Override
                    public com.example.backend.demand.dto.DemandDto getDetail(Long demandId) {
                        return null;
                    }

                    @Override
                    public com.example.backend.demand.dto.DemandDto update(Long demandId, Long userId, com.example.backend.demand.dto.DemandUpdateDto req) {
                        return null;
                    }

                    @Override
                    public void cancel(Long demandId, Long userId) {
                    }
                });

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/demands")
                        .param("category", "UNKNOWN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()));
    }

    private DemandCreateDto buildDemand(String title, DemandCategory category, RewardType rewardType) {
        DemandCreateDto dto = new DemandCreateDto();
        dto.setTitle(title);
        dto.setDescription("description");
        dto.setCategory(category);
        dto.setRewardAmount(20);
        dto.setRewardType(rewardType);
        return dto;
    }

    private ArbitrationResolveDto buildResolve(String action, String resolution) {
        ArbitrationResolveDto dto = new ArbitrationResolveDto();
        dto.setAction(action);
        dto.setResolution(resolution);
        return dto;
    }

    private static final class CertificationDtoOrThrow {
        private final Long id;

        private CertificationDtoOrThrow(com.example.backend.user.dto.CertificationDto dto) {
            this.id = dto.getId();
        }

        private Long getId() {
            return id;
        }
    }

    private static final class DemandDtoOrThrow {
        private final Long id;

        private DemandDtoOrThrow(com.example.backend.demand.dto.DemandDto dto) {
            this.id = dto.getId();
        }

        private Long getId() {
            return id;
        }
    }

    private static final class ArbitrationDtoHolder {
        private final Long id;

        private ArbitrationDtoHolder(com.example.backend.demand.dto.ArbitrationDto dto) {
            this.id = dto.getId();
        }

        private Long getId() {
            return id;
        }
    }

    private static final class DemandUpdateDtoHolder {
        private final com.example.backend.demand.dto.DemandUpdateDto value = new com.example.backend.demand.dto.DemandUpdateDto();

        private void setTitle(String title) {
            value.setTitle(title);
        }
    }
}
