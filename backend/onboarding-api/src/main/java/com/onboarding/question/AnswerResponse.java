package com.onboarding.question;

public record AnswerResponse(
        boolean isCorrect,
        String correctAnswer,
        String explanation,
        Long wrongQuestionId,
        String status,
        String nextStep
) {
}
