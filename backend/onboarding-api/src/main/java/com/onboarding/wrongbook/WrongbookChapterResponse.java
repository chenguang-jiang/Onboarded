package com.onboarding.wrongbook;

public record WrongbookChapterResponse(
        long chapterId,
        String chapterTitle,
        int count
) {
}
