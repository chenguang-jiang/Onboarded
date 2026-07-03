package com.onboarding.ai;

import java.util.List;

public record AiChatMessageResponse(
        long id,
        String role,
        String content,
        List<String> references,
        int tokensUsed,
        String createdAt
) {
}
