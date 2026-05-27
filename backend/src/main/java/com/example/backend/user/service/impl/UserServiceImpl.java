package com.example.backend.user.service.impl;

import com.example.backend.auth.security.PasswordHasher;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserRole;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.user.dto.UserCreateDto;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserPublicDto;
import com.example.backend.user.dto.UserUpdateDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto createUser(UserCreateDto req) {
        if (req == null || isBlank(req.getUsername()) || isBlank(req.getPassword())
                || isBlank(req.getStudentId()) || isBlank(req.getRealName())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "username/password/studentId/realName cannot be blank");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (userRepository.existsByStudentId(req.getStudentId())) {
            throw new BusinessException(ErrorCode.STUDENT_ID_EXISTS);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(PasswordHasher.hash(req.getPassword()));
        user.setStudentId(req.getStudentId());
        user.setRealName(req.getRealName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setAvatarUrl(null);
        user.setCreditScore(100);
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.PENDING_CERT);
        userRepository.save(user);
        return UserDto.from(user);
    }

    @Override
    public UserDto getProfile(Long userId) {
        return UserDto.from(findUser(userId));
    }

    @Override
    public UserPublicDto getPublicInfo(Long userId) {
        return UserPublicDto.from(findUser(userId));
    }

    @Override
    public UserDto updateProfile(Long userId, UserUpdateDto req) {
        if (req == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "request body cannot be blank");
        }
        User user = findUser(userId);
        if (req.getRealName() != null) {
            user.setRealName(req.getRealName());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getAvatarUrl() != null) {
            user.setAvatarUrl(req.getAvatarUrl());
        }
        userRepository.save(user);
        return UserDto.from(user);
    }

    private User findUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "userId cannot be blank");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
