package com.example.backend.demand.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserRole;
import com.example.backend.demand.dto.ReviewDto;
import com.example.backend.demand.dto.ReviewSubmitDto;
import com.example.backend.demand.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "评价", description = "提交评价、查看评价列表")
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "提交评价")
    @PostMapping
    public ApiResponse<ReviewDto> submit(@RequestBody ReviewSubmitDto request) {
        return ApiResponse.success(reviewService.submitReview(AuthContext.requireUserId(), request));
    }

    @Operation(summary = "查看用户的评价列表")
    @GetMapping("/user/{userId}")
    public ApiResponse<List<ReviewDto>> listByUser(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        Long currentUserId = AuthContext.requireUserId();
        if (!currentUserId.equals(userId) && !AuthContext.hasRole(UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return ApiResponse.success(reviewService.listByUser(userId));
    }

    @Operation(summary = "查看订单的评价")
    @GetMapping("/order/{orderId}")
    public ApiResponse<List<ReviewDto>> listByOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        return ApiResponse.success(reviewService.listByOrder(orderId, AuthContext.requireUserId()));
    }
}
