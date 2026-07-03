package com.onboarding.ai;

import java.time.Instant;

/**
 * In-memory model of an AI chat session (docs/02-backend-design.md §5.11 {@code ai_chat_session}).
 */
public record AiChatSession(
        long id,
        long userId,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
    public static AiChatSession newSession(long userId, String title, Instant now) {
        String resolved = title == null || title.isBlank() ? "新答疑" : title;
        return new AiChatSession(0L, userId, resolved, now, now);
    }

    public AiChatSession withId(long newId) {
        return new AiChatSession(newId, userId, title, createdAt, updatedAt);
    }

    public AiChatSession touch(Instant now) {
        return new AiChatSession(id, userId, title, createdAt, now);
    }
}
