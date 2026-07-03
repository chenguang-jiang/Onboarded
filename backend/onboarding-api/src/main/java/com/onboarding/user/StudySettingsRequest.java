package com.onboarding.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record StudySettingsRequest(
        @NotNull(message = "examDate is required")
        LocalDate examDate,

        @Min(value = 1, message = "dailyTarget must be at least 1")
        @Max(value = 60, message = "dailyTarget must be at most 60")
        int dailyTarget,

        @NotNull(message = "reminderTime is required")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "reminderTime must be HH:mm")
        String reminderTime
) {
}
