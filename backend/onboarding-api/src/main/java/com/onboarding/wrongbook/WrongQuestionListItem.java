package com.onboarding.wrongbook;

import com.onboarding.content.QuestionOption;

import java.util.List;

/**
 * Wrong-question list/detail item. Carries the question stem and options so the answer page can
 * render a redo, but never the {@code answerKey}.
 */
public record WrongQuestionListItem(
        long id,
        long questionId,
        long chapterId,
        String chapterTitle,
        long knowledgePointId,
        String knowledgeTitle,
        String stem,
        List<QuestionOption> options,
        int wrongCount,
        String status,
        String lastWrongAt
) {
}
