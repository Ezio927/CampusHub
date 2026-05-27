package com.example.backend.demand.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.demand.dto.ArbitrationCreateDto;
import com.example.backend.demand.dto.ArbitrationDto;
import com.example.backend.demand.dto.ArbitrationResolveDto;
import com.example.backend.demand.service.ArbitrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/arbitrations")
public class ArbitrationController {

    private final ArbitrationService arbitrationService;

    public ArbitrationController(ArbitrationService arbitrationService) {
        this.arbitrationService = arbitrationService;
    }

    @PostMapping
    public ApiResponse<ArbitrationDto> create(@RequestBody ArbitrationCreateDto request) {
        return ApiResponse.success(arbitrationService.createTicket(AuthContext.requireUserId(), request));
    }

    @GetMapping
    public ApiResponse<List<ArbitrationDto>> listPending() {
        return ApiResponse.success(arbitrationService.listPending());
    }

    @PutMapping("/{id}/resolve")
    public ApiResponse<ArbitrationDto> resolve(@PathVariable Long id, @RequestBody ArbitrationResolveDto request) {
        return ApiResponse.success(arbitrationService.resolve(id, AuthContext.requireUserId(), request));
    }
}
