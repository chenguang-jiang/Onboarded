package com.onboarding.user;

public record UserOnboardingState(
        long userId,
        boolean completed,
        String lastStep
) {

    public UserOnboardingState complete() {
        return new UserOnboardingState(userId, true, "COMPLETED");
    }
}
