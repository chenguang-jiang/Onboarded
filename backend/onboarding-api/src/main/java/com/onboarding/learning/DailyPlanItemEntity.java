package com.onboarding.learning;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

@TableName("daily_plan_item")
public class DailyPlanItemEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long userId;
    private Long knowledgePointId;
    private Long questionId;
    @TableField("source_type")
    private String source;
    private String status;
    private Integer sortNo;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public DailyPlanItemEntity() {
    }

    public static DailyPlanItemEntity from(DailyPlanItem i) {
        DailyPlanItemEntity e = new DailyPlanItemEntity();
        e.id = i.id() == 0L ? null : i.id();
        e.planId = i.planId();
        e.userId = i.userId();
        e.knowledgePointId = i.knowledgePointId();
        e.questionId = i.questionId();
        e.source = i.source();
        e.status = i.status();
        e.sortNo = i.sortNo();
        e.completedAt = i.completedAt();
        e.createdAt = i.createdAt();
        e.updatedAt = i.updatedAt();
        return e;
    }

    public DailyPlanItem toRecord() {
        return new DailyPlanItem(
                id == null ? 0L : id, planId, userId, knowledgePointId, questionId,
                source, status, sortNo == null ? 0 : sortNo, completedAt, createdAt, updatedAt
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getKnowledgePointId() { return knowledgePointId; }
    public void setKnowledgePointId(Long knowledgePointId) { this.knowledgePointId = knowledgePointId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
