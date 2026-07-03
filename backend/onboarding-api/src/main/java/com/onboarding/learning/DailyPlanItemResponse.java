package com.onboarding.learning;

import com.onboarding.content.QuestionOption;

import java.util.List;

/**
 * A today plan item. The embedded question carries stem and options only — never the answerKey —
 * so the correct answer is not shipped before submission.
 */
public record DailyPlanItemResponse(
        long itemId,
        long knowledgePointId,
        long chapterId,
        String chapterTitle,
        KnowledgeBrief knowledge,
        QuestionBrief question,
        String status,
        String source
) {
    public record KnowledgeBrief(
            long id,
            String title,
            String summary,
            String difficulty,
            List<String> tags
    ) {
    }

    public record QuestionBrief(
            long id,
            String stem,
            List<QuestionOption> options
    ) {
    }
}
