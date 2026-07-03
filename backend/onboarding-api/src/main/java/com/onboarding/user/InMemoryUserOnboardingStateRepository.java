package com.onboarding.user;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;

@Repository
@Profile("standalone")
public class InMemoryUserOnboardingStateRepository implements UserOnboardingStateRepository {

    private final Map<Long, UserOnboardingState> states = new LinkedHashMap<>();

    @Override
    public synchronized UserOnboardingState findOrCreateByUserId(long userId) {
        return states.computeIfAbsent(userId, id -> new UserOnboardingState(id, false, "WX_LOGIN"));
    }

    @Override
    public synchronized UserOnboardingState markCompleted(long userId) {
        UserOnboardingState completed = findOrCreateByUserId(userId).complete();
        states.put(userId, completed);
        return completed;
    }
}
