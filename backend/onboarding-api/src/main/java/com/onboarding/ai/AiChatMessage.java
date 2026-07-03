package com.onboarding.ai;

import java.time.Instant;
import java.util.List;

/**
 * In-memory model of an AI chat message (docs/02-backend-design.md §5.11 {@code ai_chat_message}).
 * {@code references} stands in for {@code references_json}; {@code tokensUsed} for {@code token_usage_json}.
 */
public record AiChatMessage(
        long id,
        long sessionId,
        long userId,
        String role,
        String content,
        List<String> references,
        int tokensUsed,
        Instant createdAt
) {
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    public static AiChatMessage newUser(long sessionId, long userId, String content, Instant now) {
        return new AiChatMessage(0L, sessionId, userId, ROLE_USER, content, List.of(), 0, now);
    }

    public static AiChatMessage newAssistant(
            long sessionId, long userId, String content, List<String> references, int tokensUsed, Instant now
    ) {
        return new AiChatMessage(0L, sessionId, userId, ROLE_ASSISTANT, content, references, tokensUsed, now);
    }

    public AiChatMessage withId(long newId) {
        return new AiChatMessage(newId, sessionId, userId, role, content, references, tokensUsed, createdAt);
    }
}
