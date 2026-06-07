package com.example.backend.user.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.user.dto.BanAppealDto;
import com.example.backend.user.dto.CreditAppealDto;
import com.example.backend.user.dto.CreditRecordDto;
import com.example.backend.user.dto.QuotaAccountDto;
import com.example.backend.user.dto.QuotaRecordDto;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserPublicDto;
import com.example.backend.user.dto.UserUpdateDto;
import com.example.backend.user.service.BanAppealService;
import com.example.backend.user.service.CreditService;
import com.example.backend.user.service.QuotaService;
import com.example.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Users", description = "Profile, balance, and appeal APIs")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final BanAppealService banAppealService;
    private final CreditService creditService;
    private final QuotaService quotaService;

    public UserController(UserService userService,
                          BanAppealService banAppealService,
                          CreditService creditService,
                          QuotaService quotaService) {
        this.userService = userService;
        this.banAppealService = banAppealService;
        this.creditService = creditService;
        this.quotaService = quotaService;
    }

    @Operation(summary = "Get my profile")
    @GetMapping("/me")
    public ApiResponse<UserDto> getMe() {
        return ApiResponse.success(userService.getProfile(AuthContext.requireUserId()));
    }

    @Operation(summary = "Update my profile")
    @PutMapping("/me")
    public ApiResponse<UserDto> updateMe(@RequestBody UserUpdateDto request) {
        return ApiResponse.success(userService.updateProfile(AuthContext.requireUserId(), request));
    }

    @Operation(summary = "Get public user info")
    @GetMapping("/{id}")
    public ApiResponse<UserPublicDto> getPublicInfo(
            @Parameter(description = "User ID") @PathVariable Long id) {
        return ApiResponse.success(userService.getPublicInfo(id));
    }

    @Operation(summary = "Get my credit records")
    @GetMapping("/me/credit-records")
    public ApiResponse<List<CreditRecordDto>> getMyCreditRecords() {
        return ApiResponse.success(creditService.listRecords(AuthContext.requireUserId()));
    }

    @Operation(summary = "Get my quota account")
    @GetMapping("/me/quota")
    public ApiResponse<QuotaAccountDto> getMyQuota() {
        return ApiResponse.success(quotaService.getAccount(AuthContext.requireUserId()));
    }

    @Operation(summary = "Get my quota records")
    @GetMapping("/me/quota-records")
    public ApiResponse<List<QuotaRecordDto>> getMyQuotaRecords() {
        return ApiResponse.success(quotaService.listRecords(AuthContext.requireUserId()));
    }

    @Operation(summary = "Daily check-in")
    @PostMapping("/me/quota/check-in")
    public ApiResponse<QuotaRecordDto> checkIn() {
        return ApiResponse.success(quotaService.checkIn(AuthContext.requireUserId()));
    }

    @Operation(summary = "Submit credit appeal")
    @PostMapping("/me/credit-appeal")
    public ApiResponse<CreditAppealDto> submitCreditAppeal(@RequestBody Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        return ApiResponse.success(creditService.submitAppeal(AuthContext.requireUserId(), reason));
    }

    @Operation(summary = "Get my latest credit appeal")
    @GetMapping("/me/credit-appeal")
    public ApiResponse<CreditAppealDto> getMyCreditAppeal() {
        return ApiResponse.success(creditService.getMyLatestAppeal(AuthContext.requireUserId()));
    }

    @Operation(summary = "Submit ban appeal")
    @PostMapping("/me/ban-appeal")
    public ApiResponse<BanAppealDto> submitBanAppeal(@RequestBody Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        return ApiResponse.success(banAppealService.submitAppeal(AuthContext.requireUserId(), reason));
    }

    @Operation(summary = "Get my latest ban appeal")
    @GetMapping("/me/ban-appeal")
    public ApiResponse<BanAppealDto> getMyBanAppeal() {
        return ApiResponse.success(banAppealService.getMyLatestAppeal(AuthContext.requireUserId()));
    }
}
