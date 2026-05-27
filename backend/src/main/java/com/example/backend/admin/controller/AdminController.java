package com.example.backend.admin.controller;

import com.example.backend.admin.dto.AdminUserDto;
import com.example.backend.admin.dto.BanUserDto;
import com.example.backend.admin.dto.DashboardDto;
import com.example.backend.admin.service.AdminService;
import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserRole;
import com.example.backend.demand.dto.ArbitrationDto;
import com.example.backend.demand.dto.ArbitrationResolveDto;
import com.example.backend.user.dto.CertificationDto;
import com.example.backend.user.dto.CertificationReviewDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardDto> dashboard() {
        requireAdmin();
        return ApiResponse.success(adminService.getDashboard());
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserDto>> listUsers(@RequestParam(value = "keyword", required = false) String keyword,
                                                     @RequestParam(value = "status", required = false) String status) {
        requireAdmin();
        return ApiResponse.success(adminService.listUsers(keyword, status));
    }

    @PutMapping("/users/{id}/ban")
    public ApiResponse<Void> banUser(@PathVariable Long id, @RequestBody(required = false) BanUserDto request) {
        requireAdmin();
        adminService.banUser(id, AuthContext.requireUserId(), request);
        return ApiResponse.success();
    }

    @PutMapping("/users/{id}/unban")
    public ApiResponse<Void> unbanUser(@PathVariable Long id) {
        requireAdmin();
        adminService.unbanUser(id, AuthContext.requireUserId());
        return ApiResponse.success();
    }

    @PutMapping("/certifications/{id}/review")
    public ApiResponse<CertificationDto> reviewCertification(@PathVariable Long id,
                                                             @RequestBody CertificationReviewDto request) {
        requireAdmin();
        return ApiResponse.success(adminService.reviewCertification(id, AuthContext.requireUserId(), request));
    }

    @PutMapping("/arbitrations/{id}/resolve")
    public ApiResponse<ArbitrationDto> resolveArbitration(@PathVariable Long id,
                                                          @RequestBody ArbitrationResolveDto request) {
        requireAdmin();
        return ApiResponse.success(adminService.resolveArbitration(id, AuthContext.requireUserId(), request));
    }

    private void requireAdmin() {
        if (!AuthContext.hasRole(UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
