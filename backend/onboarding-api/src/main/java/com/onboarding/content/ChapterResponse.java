package com.onboarding.content;

public record ChapterResponse(
        long id,
        String title,
        String description,
        int knowledgeCount,
        int questionCount
) {
}
