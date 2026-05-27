package com.example.backend.demand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrderCancelDto {
    @NotBlank(message = "取消原因不能为空")
    @Size(max = 500, message = "取消原因不能超过 500 字")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
