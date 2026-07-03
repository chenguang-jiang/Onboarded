package com.onboarding.notification;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

@TableName("subscription_preference")
public class SubscriptionPreferenceEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String templateId;
    private String scene;
    private Boolean accepted;
    private String status;
    private Instant lastDecisionAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static SubscriptionPreferenceEntity from(SubscriptionPreference preference) {
        SubscriptionPreferenceEntity e = new SubscriptionPreferenceEntity();
        e.id = preference.id() == 0L ? null : preference.id();
        e.userId = preference.userId();
        e.templateId = preference.templateId();
        e.scene = preference.scene();
        e.accepted = preference.accepted();
        e.status = preference.status();
        e.lastDecisionAt = preference.lastDecisionAt();
        e.createdAt = preference.createdAt();
        e.updatedAt = preference.updatedAt();
        return e;
    }

    public SubscriptionPreference toRecord() {
        return new SubscriptionPreference(
                id == null ? 0L : id,
                userId == null ? 0L : userId,
                templateId,
                scene,
                Boolean.TRUE.equals(accepted),
                status,
                lastDecisionAt,
                createdAt,
                updatedAt
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getLastDecisionAt() {
        return lastDecisionAt;
    }

    public void setLastDecisionAt(Instant lastDecisionAt) {
        this.lastDecisionAt = lastDecisionAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
