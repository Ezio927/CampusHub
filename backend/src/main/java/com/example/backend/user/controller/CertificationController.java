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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/certifications")
public class CertificationController {

    private final CertificationService certificationService;

    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @PostMapping
    public ApiResponse<CertificationDto> submit(@RequestBody CertificationSubmitDto request) {
        return ApiResponse.success(certificationService.submit(AuthContext.requireUserId(), request));
    }

    @GetMapping("/me")
    public ApiResponse<CertificationDto> getMine() {
        return ApiResponse.success(certificationService.getMyCertification(AuthContext.requireUserId()));
    }

    @PutMapping("/{id}/review")
    public ApiResponse<CertificationDto> review(@PathVariable Long id,
                                                @RequestBody CertificationReviewDto request) {
        if (!AuthContext.hasRole(UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "admin only");
        }
        boolean approve = request != null && Boolean.TRUE.equals(request.getApprove());
        String reason = request == null ? null : request.getReason();
        return ApiResponse.success(certificationService.review(id, approve, reason, AuthContext.requireUserId()));
    }
}
