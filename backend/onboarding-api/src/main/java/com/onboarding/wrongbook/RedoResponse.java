package com.onboarding.wrongbook;

public record RedoResponse(
        boolean isCorrect,
        String correctAnswer,
        String explanation,
        String status,
        boolean autoMastered,
        boolean mastered
) {
}
