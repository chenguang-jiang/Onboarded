package com.onboarding.notification;

public record SubscriptionResponse(
        long id,
        String templateId,
        String scene,
        boolean accepted,
        String status,
        String updatedAt
) {
}
