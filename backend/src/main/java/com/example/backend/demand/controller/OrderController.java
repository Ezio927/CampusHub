package com.example.backend.demand.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.demand.dto.AcceptOrderDto;
import com.example.backend.demand.dto.OrderCancelDto;
import com.example.backend.demand.dto.OrderDto;
import com.example.backend.demand.service.OrderService;
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
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderDto> accept(@RequestBody AcceptOrderDto request) {
        return ApiResponse.success(orderService.acceptDemand(AuthContext.requireUserId(), request));
    }

    @GetMapping
    public ApiResponse<List<OrderDto>> list(@RequestParam(value = "role", required = false, defaultValue = "PUBLISHER") String role,
                                            @RequestParam(value = "status", required = false) String status) {
        return ApiResponse.success(orderService.list(AuthContext.requireUserId(), role, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDto> detail(@PathVariable Long id) {
        return ApiResponse.success(orderService.getDetail(id, AuthContext.requireUserId()));
    }

    @PutMapping("/{id}/deliver")
    public ApiResponse<OrderDto> deliver(@PathVariable Long id) {
        return ApiResponse.success(orderService.deliver(id, AuthContext.requireUserId()));
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<OrderDto> confirm(@PathVariable Long id) {
        return ApiResponse.success(orderService.confirm(id, AuthContext.requireUserId()));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<OrderDto> cancel(@PathVariable Long id, @RequestBody OrderCancelDto request) {
        return ApiResponse.success(orderService.cancel(id, AuthContext.requireUserId(), request));
    }
}
