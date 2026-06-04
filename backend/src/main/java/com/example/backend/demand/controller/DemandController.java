package com.example.backend.demand.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.common.PageResult;
import com.example.backend.demand.dto.DemandCreateDto;
import com.example.backend.demand.dto.DemandDto;
import com.example.backend.demand.dto.DemandSearchCriteria;
import com.example.backend.demand.dto.DemandUpdateDto;
import com.example.backend.demand.service.DemandService;
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

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "需求", description = "发布、搜索、查看、编辑、取消需求")
@RestController
@RequestMapping("/api/demands")
public class DemandController {

    private final DemandService demandService;

    public DemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    @Operation(summary = "发布需求")
    @PostMapping
    public ApiResponse<DemandDto> create(@RequestBody DemandCreateDto request) {
        return ApiResponse.success(demandService.createDemand(AuthContext.requireUserId(), request));
    }

    @Operation(summary = "搜索需求")
    @GetMapping
    public ApiResponse<PageResult<DemandDto>> search(
            @Parameter(description = "需求分类：ERRAND/SKILL/STUDY/SECONDHAND")
            @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "需求状态：OPEN/ACCEPTED/IN_PROGRESS/COMPLETED/CANCELLED")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "关键词模糊搜索") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "位置标签") @RequestParam(value = "locationTag", required = false) String locationTag,
            @Parameter(description = "校区筛选") @RequestParam(value = "campus", required = false) String campus,
            @Parameter(description = "是否紧急") @RequestParam(value = "isUrgent", required = false) Boolean urgent,
            @Parameter(description = "排序字段：rewardAmount/urgentDeadline/createdAt/recommended")
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @Parameter(description = "排序方向：asc/desc") @RequestParam(value = "sortDir", required = false) String sortDir,
            @Parameter(description = "仅查我发布的") @RequestParam(value = "my", required = false) Boolean my,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(value = "size", defaultValue = "20") int size) {
        DemandSearchCriteria criteria = new DemandSearchCriteria();
        if (category != null) {
            criteria.setCategory(com.example.backend.common.enums.DemandCategory.valueOf(category.toUpperCase()));
        }
        if (status != null) {
            criteria.setStatus(com.example.backend.common.enums.DemandStatus.valueOf(status.toUpperCase()));
        }
        criteria.setKeyword(keyword);
        criteria.setLocationTag(locationTag);
        criteria.setCampus(campus);
        criteria.setUrgent(urgent);
        criteria.setSortBy(sortBy);
        criteria.setSortDir(sortDir);
        if (Boolean.TRUE.equals(my)) {
            criteria.setPublisherId(AuthContext.requireUserId());
        }
        if ("recommended".equalsIgnoreCase(sortBy)) {
            AuthContext.AuthUser user = AuthContext.getCurrentUser();
            if (user != null) {
                return ApiResponse.success(demandService.recommend(user.getUserId(), page, size));
            }
        }
        return ApiResponse.success(demandService.search(criteria, page, size));
    }

    @Operation(summary = "查看需求详情")
    @GetMapping("/{id}")
    public ApiResponse<DemandDto> detail(
            @Parameter(description = "需求ID") @PathVariable Long id) {
        return ApiResponse.success(demandService.getDetail(id));
    }

    @Operation(summary = "编辑需求")
    @PutMapping("/{id}")
    public ApiResponse<DemandDto> update(
            @Parameter(description = "需求ID") @PathVariable Long id,
            @RequestBody DemandUpdateDto request) {
        return ApiResponse.success(demandService.update(id, AuthContext.requireUserId(), request));
    }

    @Operation(summary = "取消需求")
    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(
            @Parameter(description = "需求ID") @PathVariable Long id) {
        demandService.cancel(id, AuthContext.requireUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除需求（仅OPEN状态可删）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "需求ID") @PathVariable Long id) {
        demandService.delete(id, AuthContext.requireUserId());
        return ApiResponse.success();
    }
}
