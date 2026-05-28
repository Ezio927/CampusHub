package com.example.backend.demand.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.demand.dto.AcceptOrderDto;
import com.example.backend.demand.dto.OrderCancelDto;
import com.example.backend.demand.dto.OrderDto;
import com.example.backend.demand.service.OrderService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "订单", description = "接单、订单列表、送达、确认完成、取消")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "接单")
    @PostMapping
    public ApiResponse<OrderDto> accept(@RequestBody AcceptOrderDto request) {
        return ApiResponse.success(orderService.acceptDemand(AuthContext.requireUserId(), request));
    }

    @Operation(summary = "订单列表")
    @GetMapping
    public ApiResponse<List<OrderDto>> list(
            @Parameter(description = "角色：PUBLISHER/ACCEPTOR") @RequestParam(value = "role", required = false, defaultValue = "PUBLISHER") String role,
            @Parameter(description = "订单状态筛选") @RequestParam(value = "status", required = false) String status) {
        return ApiResponse.success(orderService.list(AuthContext.requireUserId(), role, status));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public ApiResponse<OrderDto> detail(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        return ApiResponse.success(orderService.getDetail(id, AuthContext.requireUserId()));
    }

    @Operation(summary = "标记已送达（接单者）")
    @PutMapping("/{id}/deliver")
    public ApiResponse<OrderDto> deliver(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        return ApiResponse.success(orderService.deliver(id, AuthContext.requireUserId()));
    }

    @Operation(summary = "确认完成（发布者）")
    @PutMapping("/{id}/confirm")
    public ApiResponse<OrderDto> confirm(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        return ApiResponse.success(orderService.confirm(id, AuthContext.requireUserId()));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public ApiResponse<OrderDto> cancel(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @RequestBody OrderCancelDto request) {
        return ApiResponse.success(orderService.cancel(id, AuthContext.requireUserId(), request));
    }
}
