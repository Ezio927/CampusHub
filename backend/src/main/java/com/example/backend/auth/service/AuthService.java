package com.example.backend.auth.service;

import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.RegisterRequest;
import com.example.backend.auth.dto.TokenResponse;
import com.example.backend.user.dto.UserDto;

public interface AuthService {

    UserDto register(RegisterRequest req);

    TokenResponse login(LoginRequest req);

    void logout(String authorizationHeader);

    boolean validateToken(String token);
}
