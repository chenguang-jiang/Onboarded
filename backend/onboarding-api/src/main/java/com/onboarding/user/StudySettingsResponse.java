package com.onboarding.user;

import java.time.LocalDate;

public record StudySettingsResponse(
        LocalDate examDate,
        int dailyTarget,
        String reminderTime,
        boolean onboardingCompleted
) {
}
