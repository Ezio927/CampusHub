package com.example.backend.admin.controller;

import com.example.backend.admin.dto.AdminOrderManageDto;
import com.example.backend.admin.dto.AdminUserDto;
import com.example.backend.admin.dto.AdjustBalanceDto;
import com.example.backend.admin.dto.BanUserDto;
import com.example.backend.admin.dto.DashboardDto;
import com.example.backend.admin.dto.SendNotificationRequest;
import com.example.backend.admin.dto.RatingUpdateDto;
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
import com.example.backend.user.dto.BanAppealDto;
import com.example.backend.user.dto.CertificationDto;
import com.example.backend.user.dto.CertificationReviewDto;
import com.example.backend.user.dto.CreditAppealDto;
import com.example.backend.user.dto.CreditRecordDto;
import com.example.backend.user.dto.QuotaRecordDto;
import com.example.backend.user.service.BanAppealService;
import com.example.backend.user.service.CreditService;
import com.example.backend.user.service.QuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "管理后台", description = "数据看板、用户管理、认证审核、仲裁和举报审核")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final MessageReportService messageReportService;
    private final BanAppealService banAppealService;
    private final CreditService creditService;
    private final QuotaService quotaService;

    public AdminController(AdminService adminService,
                           MessageReportService messageReportService,
                           BanAppealService banAppealService,
                           CreditService creditService,
                           QuotaService quotaService) {
        this.adminService = adminService;
        this.messageReportService = messageReportService;
        this.banAppealService = banAppealService;
        this.creditService = creditService;
        this.quotaService = quotaService;
    }

    @Operation(summary = "Dashboard")
    @GetMapping("/dashboard")
    public ApiResponse<DashboardDto> dashboard() {
        requireAdmin();
        return ApiResponse.success(adminService.getDashboard());
    }

    @Operation(summary = "List users")
    @GetMapping("/users")
    public ApiResponse<List<AdminUserDto>> listUsers(
            @Parameter(description = "Keyword") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "Status") @RequestParam(value = "status", required = false) String status) {
        requireAdmin();
        return ApiResponse.success(adminService.listUsers(keyword, status));
    }

    @Operation(summary = "List orders")
    @GetMapping("/orders")
    public ApiResponse<List<AdminOrderManageDto>> listOrders(
            @Parameter(description = "订单或需求状态筛选") @RequestParam(value = "status", required = false) String status) {
        requireAdmin();
        return ApiResponse.success(adminService.listOrders(status));
    }

    @Operation(summary = "Delete order")
    @DeleteMapping("/orders/{id}")
    public ApiResponse<Void> deleteOrder(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        requireAdmin();
        adminService.deleteOrder(id, AuthContext.requireUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "Delete unaccepted demand")
    @DeleteMapping("/demands/{id}")
    public ApiResponse<Void> deleteDemand(
            @Parameter(description = "Demand ID") @PathVariable Long id) {
        requireAdmin();
        adminService.deleteDemand(id, AuthContext.requireUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "Ban user")
    @PutMapping("/users/{id}/ban")
    public ApiResponse<Void> banUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestBody(required = false) BanUserDto request) {
        requireAdmin();
        adminService.banUser(id, AuthContext.requireUserId(), request);
        return ApiResponse.success();
    }

    @Operation(summary = "Unban user")
    @PutMapping("/users/{id}/unban")
    public ApiResponse<Void> unbanUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        requireAdmin();
        adminService.unbanUser(id, AuthContext.requireUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "管理员调整用户评分")
    @PutMapping("/users/{id}/rating")
    public ApiResponse<AdminUserDto> updateUserRating(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody RatingUpdateDto request) {
        requireAdmin();
        return ApiResponse.success(adminService.updateUserRating(id, AuthContext.requireUserId(), request));
    }

    @Operation(summary = "Adjust user credit")
    @PutMapping("/users/{id}/credit")
    public ApiResponse<CreditRecordDto> adjustCredit(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestBody AdjustBalanceDto request) {
        requireAdmin();
        if (request == null || request.getAmount() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Amount cannot be empty");
        }
        return ApiResponse.success(creditService.adjustCreditByAdmin(
                id, request.getAmount(), request.getReason(), AuthContext.requireUserId()));
    }

    @Operation(summary = "Adjust user quota")
    @PutMapping("/users/{id}/quota")
    public ApiResponse<QuotaRecordDto> adjustQuota(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestBody AdjustBalanceDto request) {
        requireAdmin();
        if (request == null || request.getAmount() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Amount cannot be empty");
        }
        return ApiResponse.success(quotaService.adjustByAdmin(
                id, request.getAmount(), request.getReason(), AuthContext.requireUserId()));
    }

    @Operation(summary = "Review certification")
    @PutMapping("/certifications/{id}/review")
    public ApiResponse<CertificationDto> reviewCertification(
            @Parameter(description = "Certification ID") @PathVariable Long id,
            @RequestBody CertificationReviewDto request) {
        requireAdmin();
        return ApiResponse.success(adminService.reviewCertification(id, AuthContext.requireUserId(), request));
    }

    @Operation(summary = "Resolve arbitration")
    @PutMapping("/arbitrations/{id}/resolve")
    public ApiResponse<ArbitrationDto> resolveArbitration(
            @Parameter(description = "Arbitration ID") @PathVariable Long id,
            @RequestBody ArbitrationResolveDto request) {
        requireAdmin();
        return ApiResponse.success(adminService.resolveArbitration(id, AuthContext.requireUserId(), request));
    }

    @Operation(summary = "List message reports")
    @GetMapping("/message-reports")
    public ApiResponse<List<MessageReportDto>> listMessageReports() {
        requireAdmin();
        return ApiResponse.success(messageReportService.listPending());
    }

    @Operation(summary = "Review message report")
    @PutMapping("/message-reports/{id}/review")
    public ApiResponse<MessageReportDto> reviewMessageReport(
            @Parameter(description = "Report ID") @PathVariable Long id,
            @RequestBody MessageReportReviewDto request) {
        requireAdmin();
        return ApiResponse.success(messageReportService.review(id, AuthContext.requireUserId(), request));
    }

    @Operation(summary = "List pending credit appeals")
    @GetMapping("/credit-appeals")
    public ApiResponse<List<CreditAppealDto>> listCreditAppeals() {
        requireAdmin();
        return ApiResponse.success(creditService.listPendingAppeals());
    }

    @Operation(summary = "Review credit appeal")
    @PutMapping("/credit-appeals/{id}/review")
    public ApiResponse<CreditAppealDto> reviewCreditAppeal(
            @Parameter(description = "Appeal ID") @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        requireAdmin();
        Boolean approved = body != null ? body.getOrDefault("approved", false) : false;
        return ApiResponse.success(creditService.reviewAppeal(id, AuthContext.requireUserId(), approved));
    }

    @Operation(summary = "管理员发送通知（全体或指定用户）")
    @PostMapping("/notifications/send")
    public ApiResponse<Void> sendNotification(@RequestBody SendNotificationRequest request) {
        requireAdmin();
        adminService.sendNotification(request, AuthContext.requireUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "List pending ban appeals")
    @GetMapping("/ban-appeals")
    public ApiResponse<List<BanAppealDto>> listBanAppeals() {
        requireAdmin();
        return ApiResponse.success(banAppealService.listPendingAppeals());
    }

    @Operation(summary = "Review ban appeal")
    @PutMapping("/ban-appeals/{id}/review")
    public ApiResponse<BanAppealDto> reviewBanAppeal(
            @Parameter(description = "Appeal ID") @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        requireAdmin();
        Boolean approved = body != null ? body.getOrDefault("approved", false) : false;
        return ApiResponse.success(banAppealService.reviewAppeal(id, AuthContext.requireUserId(), approved));
    }

    private void requireAdmin() {
        if (!AuthContext.hasRole(UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
