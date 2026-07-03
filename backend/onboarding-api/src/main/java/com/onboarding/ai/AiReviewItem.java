package com.onboarding.ai;

import java.time.Instant;
import java.util.List;

public record AiReviewItem(
        long id,
        long userId,
        long messageId,
        String content,
        List<String> references,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static final String STATUS_PENDING = "PENDING";

    public static AiReviewItem pending(long userId, AiChatMessage message, Instant now) {
        return new AiReviewItem(
                0L,
                userId,
                message.id(),
                message.content(),
                message.references(),
                STATUS_PENDING,
                now,
                now
        );
    }

    public AiReviewItem withId(long newId) {
        return new AiReviewItem(newId, userId, messageId, content, references, status, createdAt, updatedAt);
    }
}
