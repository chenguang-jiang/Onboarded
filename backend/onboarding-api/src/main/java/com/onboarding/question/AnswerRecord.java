package com.onboarding.question;

import java.time.Instant;

/**
 * In-memory model of an answer submission (docs/02-backend-design.md §5.8 {@code answer_record}).
 * {@code errorReason} is left null in M2; error-reason classification is deferred.
 */
public record AnswerRecord(
        long id,
        long userId,
        long questionId,
        String selectedAnswer,
        boolean isCorrect,
        Integer durationSec,
        String errorReason,
        Instant createdAt
) {
    public AnswerRecord withId(long id) {
        return new AnswerRecord(id, userId, questionId, selectedAnswer, isCorrect, durationSec, errorReason, createdAt);
    }
}
