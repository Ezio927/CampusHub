package com.example.backend.auth.service.impl;

import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.RegisterRequest;
import com.example.backend.auth.dto.TokenResponse;
import com.example.backend.auth.security.JwtTokenProvider;
import com.example.backend.auth.security.LoginRateLimiter;
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
    private final LoginRateLimiter loginRateLimiter;

    public AuthServiceImpl(UserRepository userRepository,
                           UserService userService,
                           JwtTokenProvider jwtTokenProvider,
                           LoginRateLimiter loginRateLimiter) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginRateLimiter = loginRateLimiter;
    }

    @Override
    public UserDto register(RegisterRequest req) {
        if (req == null || isBlank(req.getUsername()) || isBlank(req.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名/密码不能为空");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (!isBlank(req.getStudentId()) && userRepository.existsByStudentId(req.getStudentId())) {
            throw new BusinessException(ErrorCode.STUDENT_ID_EXISTS);
        }

        UserCreateDto createDto = new UserCreateDto();
        createDto.setUsername(req.getUsername());
        createDto.setPassword(req.getPassword());
        createDto.setStudentId(req.getStudentId());
        createDto.setRealName(req.getRealName());
        createDto.setGender(req.getGender());
        createDto.setPhone(req.getPhone());
        createDto.setEmail(req.getEmail());
        return userService.createUser(createDto);
    }

    @Override
    public TokenResponse login(LoginRequest req) {
        if (req == null || isBlank(req.getUsername()) || isBlank(req.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名/密码不能为空");
        }

        if (loginRateLimiter.isBlocked(req.getUsername())) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, "登录尝试过于频繁，请15分钟后再试");
        }

        User user = userRepository.findByUsername(req.getUsername())
                .orElse(null);
        if (user == null || !PasswordHasher.matches(req.getPassword(), user.getPasswordHash())) {
            loginRateLimiter.recordFailure(req.getUsername());
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户名或密码错误");
        }

        loginRateLimiter.clear(req.getUsername());

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
