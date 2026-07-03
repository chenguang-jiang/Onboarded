package com.onboarding.progress;

import java.time.Instant;

/**
 * In-memory model of per-knowledge-point mastery (docs/02-backend-design.md §5.10 {@code user_mastery}).
 * Scoring follows docs/01-product-plan.md §6.6: practice correct +15, first wrong -15, repeat wrong -10,
 * redo correct +20, redo wrong -10. Scores are clamped to 0..100. {@code nextReviewAt} is left null in M3;
 * the 7-day low-frequency review schedule is deferred.
 */
public record UserMastery(
        long id,
        long userId,
        long knowledgePointId,
        int masteryScore,
        int studyCount,
        int correctCount,
        int wrongCount,
        Instant lastReviewAt,
        Instant nextReviewAt,
        Instant createdAt,
        Instant updatedAt
) {
    private static int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }

    public static UserMastery newMastery(long userId, long knowledgePointId, Instant now) {
        return new UserMastery(0L, userId, knowledgePointId, 0, 0, 0, 0, null, null, now, now);
    }

    public UserMastery applyPracticeCorrect(Instant now) {
        return new UserMastery(
                id, userId, knowledgePointId,
                clamp(masteryScore + 15),
                studyCount + 1, correctCount + 1, wrongCount,
                now, nextReviewAt, createdAt, now
        );
    }

    public UserMastery applyPracticeWrong(Instant now) {
        int delta = wrongCount == 0 ? -15 : -10;
        return new UserMastery(
                id, userId, knowledgePointId,
                clamp(masteryScore + delta),
                studyCount + 1, correctCount, wrongCount + 1,
                now, nextReviewAt, createdAt, now
        );
    }

    public UserMastery applyRedoCorrect(Instant now) {
        return new UserMastery(
                id, userId, knowledgePointId,
                clamp(masteryScore + 20),
                studyCount + 1, correctCount + 1, wrongCount,
                now, nextReviewAt, createdAt, now
        );
    }

    public UserMastery applyRedoWrong(Instant now) {
        return new UserMastery(
                id, userId, knowledgePointId,
                clamp(masteryScore - 10),
                studyCount + 1, correctCount, wrongCount + 1,
                now, nextReviewAt, createdAt, now
        );
    }

    public UserMastery withId(long newId) {
        return new UserMastery(
                newId, userId, knowledgePointId,
                masteryScore, studyCount, correctCount, wrongCount,
                lastReviewAt, nextReviewAt, createdAt, updatedAt
        );
    }
}
