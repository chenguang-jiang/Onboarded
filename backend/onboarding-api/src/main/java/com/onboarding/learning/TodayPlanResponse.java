package com.onboarding.learning;

import java.util.List;

public record TodayPlanResponse(
        long planId,
        String planDate,
        int totalCount,
        int completedCount,
        List<DailyPlanItemResponse> items
) {
}
