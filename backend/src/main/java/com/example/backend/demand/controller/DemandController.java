package com.example.backend.demand.controller;

import com.example.backend.auth.security.AuthContext;
import com.example.backend.common.ApiResponse;
import com.example.backend.demand.dto.DemandCreateDto;
import com.example.backend.demand.dto.DemandDto;
import com.example.backend.demand.dto.DemandSearchCriteria;
import com.example.backend.demand.dto.DemandUpdateDto;
import com.example.backend.demand.service.DemandService;
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
@RequestMapping("/api/demands")
public class DemandController {

    private final DemandService demandService;

    public DemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    @PostMapping
    public ApiResponse<DemandDto> create(@RequestBody DemandCreateDto request) {
        return ApiResponse.success(demandService.createDemand(AuthContext.requireUserId(), request));
    }

    @GetMapping
    public ApiResponse<List<DemandDto>> search(@RequestParam(value = "category", required = false) String category,
                                               @RequestParam(value = "status", required = false) String status,
                                               @RequestParam(value = "keyword", required = false) String keyword,
                                               @RequestParam(value = "locationTag", required = false) String locationTag,
                                               @RequestParam(value = "isUrgent", required = false) Boolean urgent,
                                               @RequestParam(value = "sortBy", required = false) String sortBy,
                                               @RequestParam(value = "sortDir", required = false) String sortDir) {
        DemandSearchCriteria criteria = new DemandSearchCriteria();
        if (category != null) {
            criteria.setCategory(com.example.backend.common.enums.DemandCategory.valueOf(category.toUpperCase()));
        }
        if (status != null) {
            criteria.setStatus(com.example.backend.common.enums.DemandStatus.valueOf(status.toUpperCase()));
        }
        criteria.setKeyword(keyword);
        criteria.setLocationTag(locationTag);
        criteria.setUrgent(urgent);
        criteria.setSortBy(sortBy);
        criteria.setSortDir(sortDir);
        return ApiResponse.success(demandService.search(criteria));
    }

    @GetMapping("/{id}")
    public ApiResponse<DemandDto> detail(@PathVariable Long id) {
        return ApiResponse.success(demandService.getDetail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<DemandDto> update(@PathVariable Long id, @RequestBody DemandUpdateDto request) {
        return ApiResponse.success(demandService.update(id, AuthContext.requireUserId(), request));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        demandService.cancel(id, AuthContext.requireUserId());
        return ApiResponse.success();
    }
}
