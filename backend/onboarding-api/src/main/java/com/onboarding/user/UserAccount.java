package com.onboarding.user;

import java.time.LocalDate;
import java.time.LocalTime;

public record UserAccount(
        long id,
        String openid,
        String unionid,
        LocalDate examDate,
        int dailyTarget,
        LocalTime reminderTime
) {

    public UserAccount withStudySettings(LocalDate newExamDate, int newDailyTarget, LocalTime newReminderTime) {
        return new UserAccount(id, openid, unionid, newExamDate, newDailyTarget, newReminderTime);
    }
}
