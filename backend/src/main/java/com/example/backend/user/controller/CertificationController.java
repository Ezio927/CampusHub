package com.example.backend.user.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserRole;
import com.example.backend.user.dto.CertificationDto;
import com.example.backend.user.dto.CertificationReviewDto;
import com.example.backend.user.dto.CertificationSubmitDto;
import com.example.backend.user.service.CertificationService;
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

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "实名认证", description = "提交认证、查看状态、审核认证")
@RestController
@RequestMapping("/api/certifications")
public class CertificationController {

    private final CertificationService certificationService;

    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @Operation(summary = "提交实名认证")
    @PostMapping
    public ApiResponse<CertificationDto> submit(@RequestBody CertificationSubmitDto request) {
        return ApiResponse.success(certificationService.submit(AuthContext.requireUserId(), request));
    }

    @Operation(summary = "查看我的认证状态")
    @GetMapping("/me")
    public ApiResponse<CertificationDto> getMine() {
        return ApiResponse.success(certificationService.getMyCertification(AuthContext.requireUserId()));
    }

    @Operation(summary = "审核认证（管理员）")
    @PutMapping("/{id}/review")
    public ApiResponse<CertificationDto> review(
            @Parameter(description = "认证ID") @PathVariable Long id,
            @RequestBody CertificationReviewDto request) {
        if (!AuthContext.hasRole(UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅管理员可操作");
        }
        boolean approve = request != null && Boolean.TRUE.equals(request.getApprove());
        String reason = request == null ? null : request.getReason();
        return ApiResponse.success(certificationService.review(id, approve, reason, AuthContext.requireUserId()));
    }
}
