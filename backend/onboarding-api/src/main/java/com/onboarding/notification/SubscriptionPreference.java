package com.onboarding.notification;

import java.time.Instant;

public record SubscriptionPreference(
        long id,
        long userId,
        String templateId,
        String scene,
        boolean accepted,
        String status,
        Instant lastDecisionAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REJECTED = "REJECTED";

    public static SubscriptionPreference newDecision(
            long userId,
            String templateId,
            String scene,
            boolean accepted,
            Instant now
    ) {
        return new SubscriptionPreference(
                0L,
                userId,
                templateId,
                scene,
                accepted,
                accepted ? STATUS_ACCEPTED : STATUS_REJECTED,
                now,
                now,
                now
        );
    }

    public SubscriptionPreference updateDecision(String templateId, boolean accepted, Instant now) {
        return new SubscriptionPreference(
                id,
                userId,
                templateId,
                scene,
                accepted,
                accepted ? STATUS_ACCEPTED : STATUS_REJECTED,
                now,
                createdAt,
                now
        );
    }

    public SubscriptionPreference withId(long newId) {
        return new SubscriptionPreference(
                newId, userId, templateId, scene, accepted, status, lastDecisionAt, createdAt, updatedAt
        );
    }
}
