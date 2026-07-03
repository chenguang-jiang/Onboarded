package com.onboarding.learning;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;
import java.time.LocalDate;

@TableName("daily_plan")
public class DailyPlanEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate planDate;
    private Integer totalCount;
    private Integer completedCount;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public DailyPlanEntity() {
    }

    public static DailyPlanEntity from(DailyPlan p) {
        DailyPlanEntity e = new DailyPlanEntity();
        e.id = p.id() == 0L ? null : p.id();
        e.userId = p.userId();
        e.planDate = p.planDate();
        e.totalCount = p.totalCount();
        e.completedCount = p.completedCount();
        e.status = p.status();
        e.createdAt = p.createdAt();
        e.updatedAt = p.updatedAt();
        return e;
    }

    public DailyPlan toRecord() {
        return new DailyPlan(
                id == null ? 0L : id, userId, planDate,
                totalCount == null ? 0 : totalCount,
                completedCount == null ? 0 : completedCount,
                status, createdAt, updatedAt
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getPlanDate() { return planDate; }
    public void setPlanDate(LocalDate planDate) { this.planDate = planDate; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
