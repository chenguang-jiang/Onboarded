package com.onboarding.learning;

import java.time.Instant;

/**
 * In-memory model of a daily plan item (docs/02-backend-design.md §5.7 {@code daily_plan_item}).
 * Status lifecycle in this milestone: PENDING -> STUDYING -> DONE. {@code questionId} is nullable for
 * {@code QUIZ_PENDING} (knowledge point without a usable question); the seed always has one question per KP,
 * so it is always set here.
 */
public record DailyPlanItem(
        long id,
        long planId,
        long userId,
        long knowledgePointId,
        Long questionId,
        String source,
        String status,
        int sortNo,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_STUDYING = "STUDYING";
    public static final String STATUS_DONE = "DONE";

    public static final String SOURCE_WRONG_RELATED = "WRONG_RELATED";
    public static final String SOURCE_LOW_MASTERY = "LOW_MASTERY";
    public static final String SOURCE_NEW = "NEW";
    public static final String SOURCE_FALLBACK = "FALLBACK";
    public static final String SOURCE_CARRY_OVER = "CARRY_OVER";

    public static DailyPlanItem newItem(
            long planId,
            long userId,
            long knowledgePointId,
            Long questionId,
            String source,
            int sortNo,
            Instant now
    ) {
        return new DailyPlanItem(0L, planId, userId, knowledgePointId, questionId,
                source, STATUS_PENDING, sortNo, null, now, now);
    }

    public DailyPlanItem withId(long newId) {
        return new DailyPlanItem(newId, planId, userId, knowledgePointId, questionId,
                source, status, sortNo, completedAt, createdAt, updatedAt);
    }

    public DailyPlanItem markStudying(Instant now) {
        if (STATUS_DONE.equals(status)) {
            return this;
        }
        return new DailyPlanItem(id, planId, userId, knowledgePointId, questionId,
                source, STATUS_STUDYING, sortNo, completedAt, createdAt, now);
    }

    public DailyPlanItem markComplete(Instant now) {
        if (STATUS_DONE.equals(status)) {
            return this;
        }
        return new DailyPlanItem(id, planId, userId, knowledgePointId, questionId,
                source, STATUS_DONE, sortNo, now, createdAt, now);
    }
}
