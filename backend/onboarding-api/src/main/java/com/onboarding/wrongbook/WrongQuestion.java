package com.onboarding.wrongbook;

import java.time.Instant;

/**
 * In-memory model of a wrong question. Extends the documented {@code wrong_question} table
 * (docs/02-backend-design.md §5.9) with {@code consecutiveCorrect} and {@code status} so the
 * "2 consecutive correct redos -> auto-mastered" rule (docs/06 §5.4) can be evaluated in memory.
 * When this moves to MySQL, add matching columns to the V2 migration.
 */
public record WrongQuestion(
        long id,
        long userId,
        long questionId,
        long chapterId,
        long knowledgePointId,
        int wrongCount,
        boolean mastered,
        int consecutiveCorrect,
        String status,
        Instant lastWrongAt,
        Instant masteredAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_RETRYING = "RETRYING";
    public static final String STATUS_MASTERED = "MASTERED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    public static WrongQuestion newOpen(
            long userId,
            long questionId,
            long chapterId,
            long knowledgePointId,
            Instant now
    ) {
        return new WrongQuestion(
                0L, userId, questionId, chapterId, knowledgePointId,
                1, false, 0, STATUS_OPEN,
                now, null, now, now
        );
    }

    /** Another wrong attempt: bump wrong count, reset consecutive-correct, reopen. */
    public WrongQuestion recordWrong(Instant now) {
        return new WrongQuestion(
                id, userId, questionId, chapterId, knowledgePointId,
                wrongCount + 1, false, 0, STATUS_OPEN,
                now, null, createdAt, now
        );
    }

    /** A correct redo: advance consecutive-correct; mastery is decided by the caller. */
    public WrongQuestion recordCorrectRedo(Instant now) {
        return new WrongQuestion(
                id, userId, questionId, chapterId, knowledgePointId,
                wrongCount, false, consecutiveCorrect + 1, STATUS_RETRYING,
                lastWrongAt, null, createdAt, now
        );
    }

    /** Mark mastered (auto or manual): hide from default list but keep the record. */
    public WrongQuestion markMastered(Instant now) {
        return new WrongQuestion(
                id, userId, questionId, chapterId, knowledgePointId,
                wrongCount, true, consecutiveCorrect, STATUS_MASTERED,
                lastWrongAt, now, createdAt, now
        );
    }

    public WrongQuestion withId(long newId) {
        return new WrongQuestion(
                newId, userId, questionId, chapterId, knowledgePointId,
                wrongCount, mastered, consecutiveCorrect, status,
                lastWrongAt, masteredAt, createdAt, updatedAt
        );
    }
}
