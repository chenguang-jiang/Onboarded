package com.onboarding.notification;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class NotificationService {

    private final SubscriptionPreferenceRepository repository;

    public NotificationService(SubscriptionPreferenceRepository repository) {
        this.repository = repository;
    }

    public SubscriptionResponse saveSubscription(long userId, SubscriptionRequest request) {
        Instant now = Instant.now();
        SubscriptionPreference preference = repository.findByUserAndScene(userId, request.scene())
                .map(existing -> existing.updateDecision(request.templateId(), request.accepted(), now))
                .orElseGet(() -> SubscriptionPreference.newDecision(
                        userId, request.templateId(), request.scene(), request.accepted(), now
                ));
        return toResponse(repository.save(preference));
    }

    private SubscriptionResponse toResponse(SubscriptionPreference preference) {
        return new SubscriptionResponse(
                preference.id(),
                preference.templateId(),
                preference.scene(),
                preference.accepted(),
                preference.status(),
                preference.updatedAt().toString()
        );
    }
}
