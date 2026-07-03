package com.onboarding.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("user_onboarding_state")
public class UserOnboardingStateEntity {

    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    private Boolean completed;
    private String lastStep;

    public UserOnboardingStateEntity() {
    }

    public static UserOnboardingStateEntity from(UserOnboardingState s) {
        UserOnboardingStateEntity e = new UserOnboardingStateEntity();
        e.userId = s.userId();
        e.completed = s.completed();
        e.lastStep = s.lastStep();
        return e;
    }

    public UserOnboardingState toRecord() {
        return new UserOnboardingState(userId == null ? 0L : userId, Boolean.TRUE.equals(completed), lastStep);
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
    public String getLastStep() { return lastStep; }
    public void setLastStep(String lastStep) { this.lastStep = lastStep; }
}
