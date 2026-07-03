package com.onboarding.ai;

import java.util.List;

/**
 * Result of a GLM knowledge-base call: the assistant content, citation sources, token usage,
 * and whether the answer was grounded in the knowledge base.
 */
public record GlmAnswer(
        String content,
        List<String> references,
        int tokensUsed,
        boolean fromKnowledgeBase
) {
}
