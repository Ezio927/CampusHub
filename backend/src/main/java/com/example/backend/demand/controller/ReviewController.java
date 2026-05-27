package com.example.backend.demand.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.demand.dto.ReviewDto;
import com.example.backend.demand.dto.ReviewSubmitDto;
import com.example.backend.demand.service.ReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ApiResponse<ReviewDto> submit(@RequestBody ReviewSubmitDto request) {
        return ApiResponse.success(reviewService.submitReview(AuthContext.requireUserId(), request));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<ReviewDto>> listByUser(@PathVariable Long userId) {
        return ApiResponse.success(reviewService.listByUser(userId));
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<List<ReviewDto>> listByOrder(@PathVariable Long orderId) {
        return ApiResponse.success(reviewService.listByOrder(orderId, AuthContext.requireUserId()));
    }
}
