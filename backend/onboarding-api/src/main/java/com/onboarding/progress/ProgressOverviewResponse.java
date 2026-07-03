package com.onboarding.progress;

public record ProgressOverviewResponse(
        int totalKnowledgePoints,
        int studiedCount,
        int masteredCount,
        int weakCount,
        int averageScore
) {
}
