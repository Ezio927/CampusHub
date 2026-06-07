package com.example.backend.user.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.moderation.ContentModerationService;
import com.example.backend.user.dto.UserCreateDto;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserUpdateDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.QuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ContentModerationService contentModerationService;
    @Mock private QuotaService quotaService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, contentModerationService, quotaService);
    }

    @Test
    void shouldCreateUser() {
        doNothing().when(contentModerationService).assertUsernameAllowed(anyString());

        UserCreateDto req = new UserCreateDto();
        req.setUsername("alice");
        req.setPassword("pass123");
        req.setStudentId("201250001");
        req.setRealName("Alice");
        req.setGender("MALE");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByStudentId(anyString())).thenReturn(false);
        doAnswer(inv -> { User u = inv.getArgument(0); u.setId(1L); return null; }).when(userRepository).save(any(User.class));
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "alice")));

        UserDto result = userService.createUser(req);
        assertNotNull(result);
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    void shouldRejectBlankUsername() {
        UserCreateDto req = new UserCreateDto();
        req.setUsername("");
        req.setPassword("pw");
        req.setStudentId("S1");
        req.setRealName("X");
        assertThrows(BusinessException.class, () -> userService.createUser(req));
    }

    @Test
    void shouldRejectBlankStudentId() {
        UserCreateDto req = new UserCreateDto();
        req.setUsername("u");
        req.setPassword("pw");
        req.setStudentId("");
        req.setRealName("X");
        assertThrows(BusinessException.class, () -> userService.createUser(req));
    }

    @Test
    void shouldUpdateProfile() {
        User u = createUser(1L, "alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        UserUpdateDto req = new UserUpdateDto();
        req.setPhone("13800001111");
        req.setGender("FEMALE");

        UserDto result = userService.updateProfile(1L, req);
        assertNotNull(result);
        verify(userRepository).save(u);
    }

    @Test
    void shouldRejectDuplicateUsernameOnUpdate() {
        User u = createUser(1L, "alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        UserUpdateDto req = new UserUpdateDto();
        req.setUsername("bob");

        assertThrows(BusinessException.class, () -> userService.updateProfile(1L, req));
    }

    @Test
    void shouldGetProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "alice")));
        UserDto dto = userService.getProfile(1L);
        assertNotNull(dto);
        assertEquals("alice", dto.getUsername());
    }

    @Test
    void shouldThrowForMissingUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> userService.getProfile(999L));
    }

    private User createUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setRealName("Test");
        u.setStudentId("S" + id);
        u.setPasswordHash("hash");
        return u;
    }
}
