package com.example.backend.boundary;

import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.RegisterRequest;
import com.example.backend.auth.security.AuthContext;
import com.example.backend.auth.security.JwtTokenProvider;
import com.example.backend.auth.security.PasswordHasher;
import com.example.backend.auth.service.impl.AuthServiceImpl;
import com.example.backend.common.ApiResponse;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.GlobalExceptionHandler;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.config.JwtConfig;
import com.example.backend.user.controller.UserController;
import com.example.backend.user.dto.UserCreateDto;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserPublicDto;
import com.example.backend.user.dto.UserUpdateDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.CreditService;
import com.example.backend.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

class MemberABoundaryTest {

    // ==========================================
    // PasswordHasher
    // ==========================================

    @Test
    void shouldHashAndVerifyPasswords() {
        String raw = "alice123";

        String hashed = PasswordHasher.hash(raw);
        Assertions.assertNotNull(hashed);
        Assertions.assertNotEquals(raw, hashed);
        Assertions.assertTrue(hashed.contains(":"), "format should be salt:hash");

        Assertions.assertTrue(PasswordHasher.matches(raw, hashed));
        Assertions.assertFalse(PasswordHasher.matches("wrong-password", hashed));
        Assertions.assertFalse(PasswordHasher.matches(null, hashed));
        Assertions.assertFalse(PasswordHasher.matches(raw, null));
        Assertions.assertFalse(PasswordHasher.matches(null, null));

        String hashed2 = PasswordHasher.hash(raw);
        Assertions.assertNotEquals(hashed, hashed2, "different salts should produce different hashes");
    }

    // ==========================================
    // JwtTokenProvider
    // ==========================================

    @Test
    void shouldGenerateAndValidateTokens() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret-for-member-a");
        jwtConfig.setExpirationMinutes(60);
        JwtTokenProvider provider = new JwtTokenProvider(jwtConfig);

        String token = provider.generateToken(1L, "alice", UserRole.STUDENT);
        Assertions.assertNotNull(token);
        Assertions.assertTrue(token.contains("."), "token should have 3 parts separated by dots");

        Assertions.assertEquals(1L, provider.parseUserId(token));
        Assertions.assertEquals("alice", provider.parseUsername(token));
        Assertions.assertEquals(UserRole.STUDENT, provider.parseRole(token));
        Assertions.assertTrue(provider.validate(token));
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret");
        jwtConfig.setExpirationMinutes(-1);
        JwtTokenProvider provider = new JwtTokenProvider(jwtConfig);

        String token = provider.generateToken(1L, "alice", UserRole.STUDENT);
        Assertions.assertFalse(provider.validate(token));
    }

    @Test
    void shouldRejectTamperedToken() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret");
        jwtConfig.setExpirationMinutes(60);
        JwtTokenProvider provider = new JwtTokenProvider(jwtConfig);

        String token = provider.generateToken(1L, "alice", UserRole.STUDENT);
        String tampered = token.substring(0, token.length() - 1) + (token.charAt(token.length() - 1) == 'A' ? 'B' : 'A');
        Assertions.assertFalse(provider.validate(tampered));
    }

    @Test
    void shouldRejectTokenWithWrongSecret() {
        JwtConfig configA = new JwtConfig();
        configA.setSecret("secret-a");
        configA.setExpirationMinutes(60);
        JwtTokenProvider providerA = new JwtTokenProvider(configA);

        JwtConfig configB = new JwtConfig();
        configB.setSecret("secret-b-different");
        configB.setExpirationMinutes(60);
        JwtTokenProvider providerB = new JwtTokenProvider(configB);

        String token = providerA.generateToken(1L, "alice", UserRole.STUDENT);
        Assertions.assertFalse(providerB.validate(token));
    }

    // ==========================================
    // AuthContext
    // ==========================================

    @Test
    void shouldManageAuthContext() {
        AuthContext.setCurrentUser(new AuthContext.AuthUser(1L, "alice", UserRole.STUDENT));
        try {
            Assertions.assertEquals(1L, AuthContext.requireUserId());
            Assertions.assertEquals("alice", AuthContext.requireCurrentUser().getUsername());
            Assertions.assertEquals(UserRole.STUDENT, AuthContext.requireCurrentUser().getRole());
            Assertions.assertTrue(AuthContext.hasRole(UserRole.STUDENT));
            Assertions.assertFalse(AuthContext.hasRole(UserRole.ADMIN));
        } finally {
            AuthContext.clear();
        }
    }

    @Test
    void shouldThrowWhenAuthContextNotSet() {
        Assertions.assertNull(AuthContext.getCurrentUser());
        Assertions.assertEquals(ErrorCode.UNAUTHORIZED.getCode(),
                Assertions.assertThrows(BusinessException.class, AuthContext::requireCurrentUser).getCode());
        Assertions.assertEquals(ErrorCode.UNAUTHORIZED.getCode(),
                Assertions.assertThrows(BusinessException.class, AuthContext::requireUserId).getCode());
    }

    @Test
    void shouldClearAuthContext() {
        AuthContext.setCurrentUser(new AuthContext.AuthUser(1L, "alice", UserRole.STUDENT));
        AuthContext.clear();
        Assertions.assertNull(AuthContext.getCurrentUser());
    }

    // ==========================================
    // User entity
    // ==========================================

    @Test
    void shouldHandleUserCreditCorrectly() {
        User user = new User();
        Assertions.assertEquals(100, user.getCreditScore());
        Assertions.assertEquals(UserRole.STUDENT, user.getRole());
        Assertions.assertEquals(UserStatus.PENDING_CERT, user.getStatus());

        Assertions.assertTrue(user.isCreditSufficient());

        user.setCreditScore(60);
        Assertions.assertTrue(user.isCreditSufficient());

        user.setCreditScore(59);
        Assertions.assertFalse(user.isCreditSufficient());
        Assertions.assertEquals(59, user.getCreditScore());

        user.addCredit(10);
        Assertions.assertEquals(69, user.getCreditScore());

        user.addCredit(-5);
        Assertions.assertEquals(69, user.getCreditScore(), "negative add should be ignored");

        user.addCredit(0);
        Assertions.assertEquals(69, user.getCreditScore(), "zero add should be ignored");

        user.deductCredit(20);
        Assertions.assertEquals(49, user.getCreditScore());

        user.deductCredit(-5);
        Assertions.assertEquals(49, user.getCreditScore(), "negative deduct should be ignored");

        user.deductCredit(100);
        Assertions.assertEquals(0, user.getCreditScore(), "should floor at 0");
    }

    // ==========================================
    // ApiResponse
    // ==========================================

    @Test
    void shouldBuildApiResponsesCorrectly() {
        ApiResponse<String> success = ApiResponse.success("hello");
        Assertions.assertEquals(20000, success.getCode());
        Assertions.assertEquals("success", success.getMessage());
        Assertions.assertEquals("hello", success.getData());

        ApiResponse<Void> empty = ApiResponse.success();
        Assertions.assertEquals(20000, empty.getCode());
        Assertions.assertNull(empty.getData());

        ApiResponse<Void> error = ApiResponse.error(ErrorCode.USER_NOT_FOUND);
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), error.getCode());
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), error.getMessage());
        Assertions.assertNull(error.getData());

        ApiResponse<Void> customError = ApiResponse.error(40001, "custom message");
        Assertions.assertEquals(40001, customError.getCode());
        Assertions.assertEquals("custom message", customError.getMessage());
    }

    // ==========================================
    // BusinessException
    // ==========================================

    @Test
    void shouldCreateBusinessExceptionWithVariousConstructors() {
        BusinessException e1 = new BusinessException(ErrorCode.USER_NOT_FOUND);
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), e1.getCode());
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), e1.getMessage());

        BusinessException e2 = new BusinessException(ErrorCode.BAD_REQUEST, "extra detail");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(), e2.getCode());
        Assertions.assertEquals("extra detail", e2.getMessage());

        BusinessException e3 = new BusinessException(49999, "raw code");
        Assertions.assertEquals(49999, e3.getCode());
        Assertions.assertEquals("raw code", e3.getMessage());
    }

    // ==========================================
    // GlobalExceptionHandler
    // ==========================================

    @Test
    void shouldHandleBusinessException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BusinessException ex = new BusinessException(ErrorCode.USERNAME_EXISTS);

        ResponseEntity<ApiResponse<Void>> entity = handler.handleBusinessException(ex);
        ApiResponse<Void> body = entity.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(ErrorCode.USERNAME_EXISTS.getCode(), body.getCode());
        Assertions.assertEquals(ErrorCode.USERNAME_EXISTS.getMessage(), body.getMessage());
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse<Void>> entity = handler.handleIllegalArgumentException(new IllegalArgumentException("bad arg"));
        ApiResponse<Void> body = entity.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(), body.getCode());
    }

    @Test
    void shouldHandleGenericException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse<Void>> entity = handler.handleException(new RuntimeException("boom"));
        ApiResponse<Void> body = entity.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(ErrorCode.SERVER_ERROR.getCode(), body.getCode());
    }

    // ==========================================
    // UserServiceImpl
    // ==========================================

    @Test
    void shouldRejectBlankUserInputs() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);

        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> userService.createUser(null)).getCode());

        UserCreateDto blank = new UserCreateDto();
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> userService.createUser(blank)).getCode());

        UserCreateDto missingPassword = new UserCreateDto();
        missingPassword.setUsername("test");
        missingPassword.setStudentId("S001");
        missingPassword.setRealName("Test");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> userService.createUser(missingPassword)).getCode());

        UserCreateDto missingRealName = new UserCreateDto();
        missingRealName.setUsername("test");
        missingRealName.setPassword("pwd");
        missingRealName.setStudentId("S001");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> userService.createUser(missingRealName)).getCode());
    }

    @Test
    void shouldRejectDuplicateUserFields() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);

        UserCreateDto duplicateUsername = new UserCreateDto();
        duplicateUsername.setUsername("alice");
        duplicateUsername.setPassword("pwd12345");
        duplicateUsername.setStudentId("2024999001");
        duplicateUsername.setRealName("Another Alice");
        Assertions.assertEquals(ErrorCode.USERNAME_EXISTS.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> userService.createUser(duplicateUsername)).getCode());

        UserCreateDto duplicateStudentId = new UserCreateDto();
        duplicateStudentId.setUsername("newuser");
        duplicateStudentId.setPassword("pwd12345");
        duplicateStudentId.setStudentId("2024001001");
        duplicateStudentId.setRealName("New User");
        Assertions.assertEquals(ErrorCode.STUDENT_ID_EXISTS.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> userService.createUser(duplicateStudentId)).getCode());
    }

    @Test
    void shouldCreateAndQueryUsers() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);

        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("bob");
        dto.setPassword("bob12345");
        dto.setStudentId("2024002001");
        dto.setRealName("Bob Tester");
        dto.setPhone("13800001111");
        dto.setEmail("bob@test.com");

        UserDto created = userService.createUser(dto);
        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals("bob", created.getUsername());
        Assertions.assertEquals("Bob Tester", created.getRealName());
        Assertions.assertEquals("13800001111", created.getPhone());
        Assertions.assertEquals("bob@test.com", created.getEmail());
        Assertions.assertEquals(100, created.getCreditScore());
        Assertions.assertEquals(UserRole.STUDENT, created.getRole());
        Assertions.assertEquals(UserStatus.PENDING_CERT, created.getStatus());

        UserDto profile = userService.getProfile(created.getId());
        Assertions.assertEquals("bob", profile.getUsername());
        Assertions.assertEquals("13800001111", profile.getPhone());

        UserPublicDto publicInfo = userService.getPublicInfo(created.getId());
        Assertions.assertEquals("bob", publicInfo.getUsername());
        Assertions.assertEquals("138****1111", publicInfo.getPhone());
        Assertions.assertEquals("b***b@test.com", publicInfo.getEmail());
    }

    @Test
    void shouldUpdateUserProfilePartially() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);

        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("charlie");
        dto.setPassword("charlie123");
        dto.setStudentId("2024003001");
        dto.setRealName("Charlie");
        dto.setPhone("13900002222");
        userService.createUser(dto);

        User alice = userRepository.findByUsername("alice").orElseThrow();
        String originalPhone = alice.getPhone();

        UserUpdateDto update = new UserUpdateDto();
        update.setRealName("Alice Updated");
        UserDto updated = userService.updateProfile(alice.getId(), update);
        Assertions.assertEquals("Alice Updated", updated.getRealName());
        Assertions.assertEquals(originalPhone, updated.getPhone(), "phone should remain unchanged");

        UserUpdateDto phoneOnly = new UserUpdateDto();
        phoneOnly.setPhone("13700003333");
        UserDto updated2 = userService.updateProfile(alice.getId(), phoneOnly);
        Assertions.assertEquals("Alice Updated", updated2.getRealName(), "realName should not revert");
        Assertions.assertEquals("13700003333", updated2.getPhone());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);

        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> userService.getProfile(9999L)).getCode());

        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> userService.getPublicInfo(9999L)).getCode());

        UserUpdateDto update = new UserUpdateDto();
        update.setRealName("x");
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> userService.updateProfile(9999L, update)).getCode());
    }

    @Test
    void shouldRejectUpdateWithNullBody() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);

        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> userService.updateProfile(1L, null)).getCode());
    }

    // ==========================================
    // AuthServiceImpl boundary
    // ==========================================

    @Test
    void shouldRejectAuthBoundaryInputs() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret");
        jwtConfig.setExpirationMinutes(60);
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtConfig);
        AuthServiceImpl authService = new AuthServiceImpl(userRepository, userService, jwtTokenProvider);

        RegisterRequest blank = new RegisterRequest();
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.register(blank)).getCode());

        RegisterRequest dupUsername = new RegisterRequest();
        dupUsername.setUsername("alice");
        dupUsername.setPassword("pwd12345");
        dupUsername.setStudentId("2024999001");
        dupUsername.setRealName("Alice Two");
        Assertions.assertEquals(ErrorCode.USERNAME_EXISTS.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.register(dupUsername)).getCode());

        RegisterRequest dupStudentId = new RegisterRequest();
        dupStudentId.setUsername("newuser");
        dupStudentId.setPassword("pwd12345");
        dupStudentId.setStudentId("2024001001");
        dupStudentId.setRealName("Alice Three");
        Assertions.assertEquals(ErrorCode.STUDENT_ID_EXISTS.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.register(dupStudentId)).getCode());

        LoginRequest blankLogin = new LoginRequest();
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.login(blankLogin)).getCode());

        LoginRequest wrongPwd = new LoginRequest();
        wrongPwd.setUsername("alice");
        wrongPwd.setPassword("wrong-password");
        Assertions.assertEquals(ErrorCode.UNAUTHORIZED.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.login(wrongPwd)).getCode());

        LoginRequest nonexistent = new LoginRequest();
        nonexistent.setUsername("no-such-user");
        nonexistent.setPassword("pwd");
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.login(nonexistent)).getCode());
    }

    @Test
    void shouldRegisterAndLoginSuccessfully() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret");
        jwtConfig.setExpirationMinutes(60);
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtConfig);
        AuthServiceImpl authService = new AuthServiceImpl(userRepository, userService, jwtTokenProvider);

        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("david");
        reg.setPassword("david12345");
        reg.setStudentId("2024004001");
        reg.setRealName("David Tester");

        UserDto registered = authService.register(reg);
        Assertions.assertNotNull(registered.getId());
        Assertions.assertEquals("david", registered.getUsername());

        LoginRequest login = new LoginRequest();
        login.setUsername("david");
        login.setPassword("david12345");
        com.example.backend.auth.dto.TokenResponse tokenResp = authService.login(login);
        Assertions.assertNotNull(tokenResp.getToken());
        Assertions.assertNotNull(tokenResp.getExpiresAt());
        Assertions.assertEquals("david", tokenResp.getUser().getUsername());

        Assertions.assertTrue(authService.validateToken(tokenResp.getToken()));
    }

    @Test
    void shouldRejectLoginWhenBanned() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret");
        jwtConfig.setExpirationMinutes(60);
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtConfig);
        AuthServiceImpl authService = new AuthServiceImpl(userRepository, userService, jwtTokenProvider);

        User alice = userRepository.findByUsername("alice").orElseThrow();
        alice.setStatus(UserStatus.BANNED);
        userRepository.save(alice);

        LoginRequest login = new LoginRequest();
        login.setUsername("alice");
        login.setPassword("alice123");
        Assertions.assertEquals(ErrorCode.ACCOUNT_BANNED.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> authService.login(login)).getCode());
    }

    // ==========================================
    // UserController
    // ==========================================

    @Test
    void shouldReturnOwnProfileWhenAuthenticated() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        CreditService creditService = new CreditService() {
            @Override
            public java.util.List<com.example.backend.user.dto.CreditRecordDto> listRecords(Long userId) {
                return Collections.emptyList();
            }

            @Override
            public boolean checkCredit(Long userId) {
                return true;
            }

            @Override
            public int getCreditScore(Long userId) {
                return 100;
            }

            @Override
            public com.example.backend.user.dto.CreditRecordDto addCredit(Long userId, int amount, String reason, Long orderId) {
                return null;
            }

            @Override
            public com.example.backend.user.dto.CreditRecordDto deductCredit(Long userId, int amount, String reason, Long orderId) {
                return null;
            }
        };
        UserController controller = new UserController(userService, creditService);

        AuthContext.setCurrentUser(new AuthContext.AuthUser(1L, "alice", UserRole.STUDENT));
        try {
            ApiResponse<UserDto> response = controller.getMe();
            Assertions.assertEquals(20000, response.getCode());
            Assertions.assertEquals("alice", response.getData().getUsername());
        } finally {
            AuthContext.clear();
        }
    }

    @Test
    void shouldReturnPublicInfoWithoutAuth() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        CreditService creditService = new CreditService() {
            @Override
            public java.util.List<com.example.backend.user.dto.CreditRecordDto> listRecords(Long userId) {
                return Collections.emptyList();
            }

            @Override
            public boolean checkCredit(Long userId) {
                return true;
            }

            @Override
            public int getCreditScore(Long userId) {
                return 100;
            }

            @Override
            public com.example.backend.user.dto.CreditRecordDto addCredit(Long userId, int amount, String reason, Long orderId) {
                return null;
            }

            @Override
            public com.example.backend.user.dto.CreditRecordDto deductCredit(Long userId, int amount, String reason, Long orderId) {
                return null;
            }
        };
        UserController controller = new UserController(userService, creditService);

        ApiResponse<UserPublicDto> response = controller.getPublicInfo(1L);
        Assertions.assertEquals(20000, response.getCode());
        Assertions.assertEquals("alice", response.getData().getUsername());
        Assertions.assertNull(response.getData().getPhone(), "seed user alice has null phone");
    }

    @Test
    void shouldReturnNotFoundForMissingUserController() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        CreditService creditService = new CreditService() {
            @Override
            public java.util.List<com.example.backend.user.dto.CreditRecordDto> listRecords(Long userId) {
                return Collections.emptyList();
            }

            @Override
            public boolean checkCredit(Long userId) {
                return true;
            }

            @Override
            public int getCreditScore(Long userId) {
                return 100;
            }

            @Override
            public com.example.backend.user.dto.CreditRecordDto addCredit(Long userId, int amount, String reason, Long orderId) {
                return null;
            }

            @Override
            public com.example.backend.user.dto.CreditRecordDto deductCredit(Long userId, int amount, String reason, Long orderId) {
                return null;
            }
        };
        UserController controller = new UserController(userService, creditService);

        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND.getCode(),
                Assertions.assertThrows(BusinessException.class, () -> controller.getPublicInfo(9999L)).getCode());
    }

    @Test
    void shouldReturnCreditRecordsWhenAuthenticated() {
        UserRepository userRepository = new UserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        CreditService creditService = new CreditService() {
            @Override
            public java.util.List<com.example.backend.user.dto.CreditRecordDto> listRecords(Long userId) {
                return Collections.emptyList();
            }

            @Override
            public boolean checkCredit(Long userId) {
                return true;
            }

            @Override
            public int getCreditScore(Long userId) {
                return 100;
            }

            @Override
            public com.example.backend.user.dto.CreditRecordDto addCredit(Long userId, int amount, String reason, Long orderId) {
                return null;
            }

            @Override
            public com.example.backend.user.dto.CreditRecordDto deductCredit(Long userId, int amount, String reason, Long orderId) {
                return null;
            }
        };
        UserController controller = new UserController(userService, creditService);

        AuthContext.setCurrentUser(new AuthContext.AuthUser(1L, "alice", UserRole.STUDENT));
        try {
            ApiResponse<?> response = controller.getMyCreditRecords();
            Assertions.assertEquals(20000, response.getCode());
        } finally {
            AuthContext.clear();
        }
    }

    @Test
    void shouldHandleGlobalExceptionHandlerViaMockMvc() throws Exception {
        com.example.backend.auth.controller.AuthController controller =
                new com.example.backend.auth.controller.AuthController(new com.example.backend.auth.service.AuthService() {
                    @Override
                    public UserDto register(RegisterRequest request) {
                        throw new BusinessException(ErrorCode.USERNAME_EXISTS);
                    }

                    @Override
                    public com.example.backend.auth.dto.TokenResponse login(LoginRequest request) {
                        throw new BusinessException(ErrorCode.UNAUTHORIZED);
                    }

                    @Override
                    public void logout(String authorization) {
                    }

                    @Override
                    public boolean validateToken(String token) {
                        return false;
                    }
                });

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"wrong\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code")
                        .value(ErrorCode.UNAUTHORIZED.getCode()));
    }

    // ==========================================
    // ErrorCode completeness
    // ==========================================

    @Test
    void shouldHaveAllRequiredErrorCodes() {
        for (ErrorCode ec : ErrorCode.values()) {
            Assertions.assertNotNull(ec.getCode(), "code must not be null: " + ec.name());
            Assertions.assertNotNull(ec.getMessage(), "message must not be null: " + ec.name());
        }
    }
}
