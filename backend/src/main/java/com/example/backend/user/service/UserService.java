package com.example.backend.user.service;

import com.example.backend.user.dto.UserCreateDto;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserPublicDto;
import com.example.backend.user.dto.UserUpdateDto;

public interface UserService {

    UserDto createUser(UserCreateDto req);

    UserDto getProfile(Long userId);

    UserPublicDto getPublicInfo(Long userId);

    UserDto updateProfile(Long userId, UserUpdateDto req);
}
