package com.example.backend.auth.service.impl;

import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.RegisterRequest;
import com.example.backend.auth.dto.TokenResponse;
import com.example.backend.auth.security.JwtTokenProvider;
import com.example.backend.auth.security.PasswordHasher;
import com.example.backend.common.BusinessException;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.user.dto.UserCreateDto;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.UserService;
import com.example.backend.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserService userService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret");
        jwtConfig.setExpirationMinutes(60);
        JwtTokenProvider jwtProvider = new JwtTokenProvider(jwtConfig);
        authService = new AuthServiceImpl(userRepository, userService, jwtProvider);
    }

    @Test
    void shouldRegisterNewUser() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("pass123");
        req.setStudentId("201250001");
        req.setRealName("Alice");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByStudentId("201250001")).thenReturn(false);
        when(userService.createUser(any(UserCreateDto.class))).thenReturn(new UserDto());

        UserDto result = authService.register(req);
        assertNotNull(result);

        ArgumentCaptor<UserCreateDto> captor = ArgumentCaptor.forClass(UserCreateDto.class);
        verify(userService).createUser(captor.capture());
        assertEquals("alice", captor.getValue().getUsername());
        assertEquals("201250001", captor.getValue().getStudentId());
    }

    @Test
    void shouldRejectDuplicateUsername() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("pass123");
        req.setStudentId("201250001");
        req.setRealName("Alice");

        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(req));
    }

    @Test
    void shouldRejectDuplicateStudentId() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("pass123");
        req.setStudentId("201250001");
        req.setRealName("Alice");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByStudentId("201250001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(req));
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest req = new LoginRequest();
        req.setUsername("bob");
        req.setPassword("secret");

        User user = new User();
        user.setId(5L);
        user.setUsername("bob");
        user.setPasswordHash(PasswordHasher.hash("secret"));
        user.setRole(UserRole.STUDENT);

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        TokenResponse result = authService.login(req);
        assertNotNull(result.getToken());
        assertEquals(5L, user.getId());
    }

    @Test
    void shouldRejectWrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setUsername("bob");
        req.setPassword("wrong");

        User user = new User();
        user.setPasswordHash(PasswordHasher.hash("secret"));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> authService.login(req));
    }

    @Test
    void shouldRejectLoginForNonExistentUser() {
        LoginRequest req = new LoginRequest();
        req.setUsername("ghost");
        req.setPassword("pw");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> authService.login(req));
    }

    @Test
    void shouldValidateToken() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test");
        jwtConfig.setExpirationMinutes(1);
        JwtTokenProvider p = new JwtTokenProvider(jwtConfig);
        AuthServiceImpl svc = new AuthServiceImpl(userRepository, userService, p);

        String token = p.generateToken(1L, "x", UserRole.STUDENT);
        assertTrue(svc.validateToken(token));
        assertFalse(svc.validateToken("bad"));
    }
}
