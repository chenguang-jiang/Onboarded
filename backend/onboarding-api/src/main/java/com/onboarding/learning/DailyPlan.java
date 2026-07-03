package com.onboarding.learning;

import java.time.Instant;
import java.time.LocalDate;

/**
 * In-memory model of a per-user, per-date daily plan (docs/02-backend-design.md §5.6 {@code daily_plan}).
 * One plan per (user, date); generated once and not regenerated the same day.
 */
public record DailyPlan(
        long id,
        long userId,
        LocalDate planDate,
        int totalCount,
        int completedCount,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static final String STATUS_ACTIVE = "ACTIVE";

    public static DailyPlan newPlan(long userId, LocalDate planDate, int totalCount, Instant now) {
        return new DailyPlan(0L, userId, planDate, totalCount, 0, STATUS_ACTIVE, now, now);
    }

    public DailyPlan withId(long newId) {
        return new DailyPlan(newId, userId, planDate, totalCount, completedCount, status, createdAt, updatedAt);
    }

    public DailyPlan withCompletedCount(int newCompletedCount, Instant now) {
        return new DailyPlan(id, userId, planDate, totalCount, newCompletedCount, status, createdAt, now);
    }
}
