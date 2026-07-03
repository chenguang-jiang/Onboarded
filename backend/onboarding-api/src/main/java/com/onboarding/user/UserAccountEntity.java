package com.onboarding.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalTime;

@TableName("user_account")
public class UserAccountEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String unionid;
    private LocalDate examDate;
    private Integer dailyTarget;
    private LocalTime reminderTime;

    public UserAccountEntity() {
    }

    public static UserAccountEntity from(UserAccount a) {
        UserAccountEntity e = new UserAccountEntity();
        e.id = a.id() == 0L ? null : a.id();
        e.openid = a.openid();
        e.unionid = a.unionid();
        e.examDate = a.examDate();
        e.dailyTarget = a.dailyTarget();
        e.reminderTime = a.reminderTime();
        return e;
    }

    public UserAccount toRecord() {
        return new UserAccount(
                id == null ? 0L : id,
                openid,
                unionid,
                examDate,
                dailyTarget == null ? 0 : dailyTarget,
                reminderTime
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public String getUnionid() { return unionid; }
    public void setUnionid(String unionid) { this.unionid = unionid; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
    public Integer getDailyTarget() { return dailyTarget; }
    public void setDailyTarget(Integer dailyTarget) { this.dailyTarget = dailyTarget; }
    public LocalTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalTime reminderTime) { this.reminderTime = reminderTime; }
}
