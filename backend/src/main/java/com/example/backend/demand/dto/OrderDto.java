package com.example.backend.demand.dto;

import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.OrderStatus;
import com.example.backend.common.enums.RewardType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDto {
    private Long id;
    private Long demandId;
    private String demandTitle;
    private String demandDescription;
    private DemandCategory category;
    private BigDecimal rewardAmount;
    private RewardType rewardType;
    private UserSummary publisher;
    private UserSummary acceptor;
    private OrderStatus status;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private LocalDateTime autoConfirmDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer creditPenalty;
    private List<StatusLogDto> statusLogs = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }

    public String getDemandTitle() {
        return demandTitle;
    }

    public void setDemandTitle(String demandTitle) {
        this.demandTitle = demandTitle;
    }

    public String getDemandDescription() {
        return demandDescription;
    }

    public void setDemandDescription(String demandDescription) {
        this.demandDescription = demandDescription;
    }

    public DemandCategory getCategory() {
        return category;
    }

    public void setCategory(DemandCategory category) {
        this.category = category;
    }

    public BigDecimal getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(BigDecimal rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public RewardType getRewardType() {
        return rewardType;
    }

    public void setRewardType(RewardType rewardType) {
        this.rewardType = rewardType;
    }

    public UserSummary getPublisher() {
        return publisher;
    }

    public void setPublisher(UserSummary publisher) {
        this.publisher = publisher;
    }

    public UserSummary getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(UserSummary acceptor) {
        this.acceptor = acceptor;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public LocalDateTime getAutoConfirmDeadline() {
        return autoConfirmDeadline;
    }

    public void setAutoConfirmDeadline(LocalDateTime autoConfirmDeadline) {
        this.autoConfirmDeadline = autoConfirmDeadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getCreditPenalty() {
        return creditPenalty;
    }

    public void setCreditPenalty(Integer creditPenalty) {
        this.creditPenalty = creditPenalty;
    }

    public List<StatusLogDto> getStatusLogs() {
        return statusLogs;
    }

    public void setStatusLogs(List<StatusLogDto> statusLogs) {
        this.statusLogs = statusLogs;
    }

    public static class UserSummary {
        private Long id;
        private String username;
        private Integer creditScore;

        public UserSummary() {
        }

        public UserSummary(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public Integer getCreditScore() {
            return creditScore;
        }

        public void setCreditScore(Integer creditScore) {
            this.creditScore = creditScore;
        }
    }

    public static class StatusLogDto {
        private OrderStatus fromStatus;
        private OrderStatus toStatus;
        private Long operatorId;
        private String remark;
        private LocalDateTime createdAt;

        public OrderStatus getFromStatus() {
            return fromStatus;
        }

        public void setFromStatus(OrderStatus fromStatus) {
            this.fromStatus = fromStatus;
        }

        public OrderStatus getToStatus() {
            return toStatus;
        }

        public void setToStatus(OrderStatus toStatus) {
            this.toStatus = toStatus;
        }

        public Long getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(Long operatorId) {
            this.operatorId = operatorId;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}
