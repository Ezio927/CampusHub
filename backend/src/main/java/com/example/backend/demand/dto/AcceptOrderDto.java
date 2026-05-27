package com.example.backend.demand.dto;

import jakarta.validation.constraints.NotNull;

public class AcceptOrderDto {
    @NotNull(message = "demandId 不能为空")
    private Long demandId;

    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }
}
