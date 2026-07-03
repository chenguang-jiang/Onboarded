package com.onboarding.notification;

import jakarta.validation.constraints.NotBlank;

public record SubscriptionRequest(
        @NotBlank String templateId,
        @NotBlank String scene,
        boolean accepted
) {
}
