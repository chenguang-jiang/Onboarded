package com.onboarding.progress;

public record ChapterProgressResponse(
        long chapterId,
        String chapterTitle,
        int totalKnowledgePoints,
        int studiedCount,
        int masteredCount,
        int weakCount,
        int averageScore,
        boolean weak
) {
}
