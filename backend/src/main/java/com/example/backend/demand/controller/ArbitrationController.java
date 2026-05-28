package com.example.backend.demand.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserRole;
import com.example.backend.demand.dto.ArbitrationCreateDto;
import com.example.backend.demand.dto.ArbitrationDto;
import com.example.backend.demand.dto.ArbitrationResolveDto;
import com.example.backend.demand.service.ArbitrationService;
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

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "仲裁", description = "创建仲裁工单、查看待处理列表、解决仲裁")
@RestController
@RequestMapping("/api/arbitrations")
public class ArbitrationController {

    private final ArbitrationService arbitrationService;

    public ArbitrationController(ArbitrationService arbitrationService) {
        this.arbitrationService = arbitrationService;
    }

    @Operation(summary = "创建仲裁工单")
    @PostMapping
    public ApiResponse<ArbitrationDto> create(@RequestBody ArbitrationCreateDto request) {
        return ApiResponse.success(arbitrationService.createTicket(AuthContext.requireUserId(), request));
    }

    @Operation(summary = "查看待处理仲裁列表（管理员）")
    @GetMapping
    public ApiResponse<List<ArbitrationDto>> listPending() {
        if (!AuthContext.hasRole(UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return ApiResponse.success(arbitrationService.listPending());
    }

    @Operation(summary = "解决仲裁工单")
    @PutMapping("/{id}/resolve")
    public ApiResponse<ArbitrationDto> resolve(
            @Parameter(description = "仲裁工单ID") @PathVariable Long id,
            @RequestBody ArbitrationResolveDto request) {
        return ApiResponse.success(arbitrationService.resolve(id, AuthContext.requireUserId(), request));
    }
}
