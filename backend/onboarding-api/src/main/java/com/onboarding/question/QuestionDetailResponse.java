package com.onboarding.question;

import com.onboarding.content.QuestionOption;

import java.util.List;

/**
 * Question payload for the answer page. Deliberately omits {@code answerKey} and
 * {@code explanation} so the correct answer is never shipped before submission.
 */
public record QuestionDetailResponse(
        long id,
        long knowledgePointId,
        String stem,
        List<QuestionOption> options
) {
}
