package com.onboarding.user;

public interface UserOnboardingStateRepository {

    UserOnboardingState findOrCreateByUserId(long userId);

    UserOnboardingState markCompleted(long userId);
}
