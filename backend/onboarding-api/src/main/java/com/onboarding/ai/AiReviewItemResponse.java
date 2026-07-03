package com.onboarding.ai;

public record AiReviewItemResponse(
        long id,
        long messageId,
        String status,
        String createdAt
) {
}
