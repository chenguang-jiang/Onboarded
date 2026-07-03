package com.onboarding.content;

import java.util.List;

public record PracticeQuestion(
        long id,
        long knowledgePointId,
        String stem,
        List<QuestionOption> options,
        String answerKey,
        String explanation
) {
}
