package com.onboarding.notification;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("standalone")
public class InMemorySubscriptionPreferenceRepository implements SubscriptionPreferenceRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, SubscriptionPreference> byId = new LinkedHashMap<>();

    @Override
    public synchronized SubscriptionPreference save(SubscriptionPreference preference) {
        SubscriptionPreference stored = preference.id() == 0L
                ? preference.withId(idGenerator.getAndIncrement())
                : preference;
        byId.put(stored.id(), stored);
        return stored;
    }

    @Override
    public synchronized Optional<SubscriptionPreference> findByUserAndScene(long userId, String scene) {
        return byId.values().stream()
                .filter(item -> item.userId() == userId && item.scene().equals(scene))
                .findFirst();
    }
}
