package com.example.backend.auth.service.impl;

import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.RegisterRequest;
import com.example.backend.auth.dto.TokenResponse;
import com.example.backend.auth.security.JwtTokenProvider;
import com.example.backend.auth.security.PasswordHasher;
import com.example.backend.auth.service.AuthService;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.user.dto.UserCreateDto;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           UserService userService,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public UserDto register(RegisterRequest req) {
        if (req == null || isBlank(req.getUsername()) || isBlank(req.getPassword())
                || isBlank(req.getStudentId()) || isBlank(req.getRealName())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名/密码/学号/姓名不能为空");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (userRepository.existsByStudentId(req.getStudentId())) {
            throw new BusinessException(ErrorCode.STUDENT_ID_EXISTS);
        }

        UserCreateDto createDto = new UserCreateDto();
        createDto.setUsername(req.getUsername());
        createDto.setPassword(req.getPassword());
        createDto.setStudentId(req.getStudentId());
        createDto.setRealName(req.getRealName());
        createDto.setPhone(req.getPhone());
        createDto.setEmail(req.getEmail());
        return userService.createUser(createDto);
    }

    @Override
    public TokenResponse login(LoginRequest req) {
        if (req == null || isBlank(req.getUsername()) || isBlank(req.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名/密码不能为空");
        }

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException(ErrorCode.ACCOUNT_BANNED);
        }
        if (!PasswordHasher.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        Instant expiresAt = Instant.ofEpochMilli(jwtTokenProvider.parseExpiresAt(token));
        return new TokenResponse(token, expiresAt, UserDto.from(user));
    }

    @Override
    public void logout(String authorizationHeader) {
        // Stateless JWT: logout is handled by client-side token removal.
    }

    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validate(token);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
