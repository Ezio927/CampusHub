package com.example.backend.user.dto;

import com.example.backend.user.entity.User;

import java.util.ArrayList;
import java.util.List;

public class QuotaAccountDto {

    private Long userId;
    private Integer available;
    private Integer frozen;
    private Integer total;
    private List<QuotaRecordDto> records = new ArrayList<QuotaRecordDto>();

    public static QuotaAccountDto from(User user, List<QuotaRecordDto> records) {
        QuotaAccountDto dto = new QuotaAccountDto();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setAvailable(user.getQuotaAvailable());
            dto.setFrozen(user.getQuotaFrozen());
            dto.setTotal(user.getQuotaAvailable() + user.getQuotaFrozen());
        }
        dto.setRecords(records);
        return dto;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getAvailable() { return available; }
    public void setAvailable(Integer available) { this.available = available; }
    public Integer getFrozen() { return frozen; }
    public void setFrozen(Integer frozen) { this.frozen = frozen; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public List<QuotaRecordDto> getRecords() { return records; }
    public void setRecords(List<QuotaRecordDto> records) {
        this.records = records == null ? new ArrayList<QuotaRecordDto>() : records;
    }
}
