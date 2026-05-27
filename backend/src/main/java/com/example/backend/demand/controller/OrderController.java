package com.example.backend.demand.controller;

import com.example.backend.common.ApiResponse;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.demand.dto.AcceptOrderDto;
import com.example.backend.demand.dto.OrderCancelDto;
import com.example.backend.demand.dto.OrderDto;
import com.example.backend.demand.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderDto> accept(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Principal principal,
            @Valid @RequestBody AcceptOrderDto request) {
        return ApiResponse.success("接单成功", orderService.acceptDemand(resolveUserId(headerUserId, principal), request));
    }

    @GetMapping
    public ApiResponse<Page<OrderDto>> list(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Principal principal,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(orderService.listMyOrders(resolveUserId(headerUserId, principal), role, status, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDto> detail(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Principal principal,
            @PathVariable Long id) {
        return ApiResponse.success(orderService.getDetail(id, resolveUserId(headerUserId, principal)));
    }

    @PutMapping("/{id}/deliver")
    public ApiResponse<OrderDto> deliver(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Principal principal,
            @PathVariable Long id) {
        return ApiResponse.success("已标记完成，等待需求方确认", orderService.markDelivered(id, resolveUserId(headerUserId, principal)));
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<OrderDto> confirm(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Principal principal,
            @PathVariable Long id) {
        return ApiResponse.success("已确认完成，请对服务方进行评价", orderService.confirmCompleted(id, resolveUserId(headerUserId, principal)));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<OrderDto> cancel(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody OrderCancelDto request) {
        OrderDto order = orderService.cancelOrder(id, resolveUserId(headerUserId, principal), request);
        String message = order.getCreditPenalty() == null || order.getCreditPenalty() == 0
                ? "已取消（无责取消，信用分不受影响）"
                : "已取消（超时取消，扣除信用分 10 分）";
        return ApiResponse.success(message, order);
    }

    private Long resolveUserId(Long headerUserId, Principal principal) {
        if (headerUserId != null) {
            return headerUserId;
        }
        if (principal != null) {
            try {
                return Long.valueOf(principal.getName());
            } catch (NumberFormatException ignored) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "无法识别当前登录用户");
            }
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}
