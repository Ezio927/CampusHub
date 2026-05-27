package com.example.backend.user.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.user.dto.CreditRecordDto;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserPublicDto;
import com.example.backend.user.dto.UserUpdateDto;
import com.example.backend.user.service.CreditService;
import com.example.backend.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CreditService creditService;

    public UserController(UserService userService, CreditService creditService) {
        this.userService = userService;
        this.creditService = creditService;
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> getMe() {
        return ApiResponse.success(userService.getProfile(AuthContext.requireUserId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserDto> updateMe(@RequestBody UserUpdateDto request) {
        return ApiResponse.success(userService.updateProfile(AuthContext.requireUserId(), request));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserPublicDto> getPublicInfo(@PathVariable Long id) {
        return ApiResponse.success(userService.getPublicInfo(id));
    }

    @GetMapping("/me/credit-records")
    public ApiResponse<List<CreditRecordDto>> getMyCreditRecords() {
        return ApiResponse.success(creditService.listRecords(AuthContext.requireUserId()));
    }
}
