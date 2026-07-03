package com.onboarding.notification;

import java.util.Optional;

public interface SubscriptionPreferenceRepository {

    SubscriptionPreference save(SubscriptionPreference preference);

    Optional<SubscriptionPreference> findByUserAndScene(long userId, String scene);
}
