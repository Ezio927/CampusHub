package com.example.backend.admin.controller;

import com.example.backend.admin.dto.AdminUserDto;
import com.example.backend.admin.dto.BanUserDto;
import com.example.backend.admin.dto.DashboardDto;
import com.example.backend.admin.service.AdminService;
import com.example.backend.auth.security.AuthContext;
import com.example.backend.chat.dto.MessageReportDto;
import com.example.backend.chat.dto.MessageReportReviewDto;
import com.example.backend.chat.service.MessageReportService;
import com.example.backend.common.ApiResponse;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserRole;
import com.example.backend.demand.dto.ArbitrationDto;
import com.example.backend.demand.dto.ArbitrationResolveDto;
import com.example.backend.user.dto.CertificationDto;
import com.example.backend.user.dto.CertificationReviewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "管理后台", description = "数据看板、用户管理、审核认证、解决仲裁（均需管理员权限）")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final MessageReportService messageReportService;

    public AdminController(AdminService adminService,
                           MessageReportService messageReportService) {
        this.adminService = adminService;
        this.messageReportService = messageReportService;
    }

    @Operation(summary = "数据看板")
    @GetMapping("/dashboard")
    public ApiResponse<DashboardDto> dashboard() {
        requireAdmin();
        return ApiResponse.success(adminService.getDashboard());
    }

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public ApiResponse<List<AdminUserDto>> listUsers(
            @Parameter(description = "关键词搜索") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "用户状态筛选") @RequestParam(value = "status", required = false) String status) {
        requireAdmin();
        return ApiResponse.success(adminService.listUsers(keyword, status));
    }

    @Operation(summary = "封禁用户")
    @PutMapping("/users/{id}/ban")
    public ApiResponse<Void> banUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody(required = false) BanUserDto request) {
        requireAdmin();
        adminService.banUser(id, AuthContext.requireUserId(), request);
        return ApiResponse.success();
    }

    @Operation(summary = "解封用户")
    @PutMapping("/users/{id}/unban")
    public ApiResponse<Void> unbanUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        requireAdmin();
        adminService.unbanUser(id, AuthContext.requireUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "审核认证")
    @PutMapping("/certifications/{id}/review")
    public ApiResponse<CertificationDto> reviewCertification(
            @Parameter(description = "认证ID") @PathVariable Long id,
            @RequestBody CertificationReviewDto request) {
        requireAdmin();
        return ApiResponse.success(adminService.reviewCertification(id, AuthContext.requireUserId(), request));
    }

    @Operation(summary = "解决仲裁")
    @PutMapping("/arbitrations/{id}/resolve")
    public ApiResponse<ArbitrationDto> resolveArbitration(
            @Parameter(description = "仲裁工单ID") @PathVariable Long id,
            @RequestBody ArbitrationResolveDto request) {
        requireAdmin();
        return ApiResponse.success(adminService.resolveArbitration(id, AuthContext.requireUserId(), request));
    }

    @Operation(summary = "待审核私信举报")
    @GetMapping("/message-reports")
    public ApiResponse<List<MessageReportDto>> listMessageReports() {
        requireAdmin();
        return ApiResponse.success(messageReportService.listPending());
    }

    @Operation(summary = "审核私信举报")
    @PutMapping("/message-reports/{id}/review")
    public ApiResponse<MessageReportDto> reviewMessageReport(
            @Parameter(description = "举报ID") @PathVariable Long id,
            @RequestBody MessageReportReviewDto request) {
        requireAdmin();
        return ApiResponse.success(messageReportService.review(id, AuthContext.requireUserId(), request));
    }

    private void requireAdmin() {
        if (!AuthContext.hasRole(UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
