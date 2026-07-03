package com.onboarding.content;

import java.util.List;

public record KnowledgePoint(
        long id,
        long chapterId,
        String title,
        String summary,
        String difficulty,
        List<String> tags
) {
}
