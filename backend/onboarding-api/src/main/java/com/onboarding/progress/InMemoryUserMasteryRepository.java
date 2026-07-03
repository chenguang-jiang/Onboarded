package com.onboarding.progress;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("standalone")
public class InMemoryUserMasteryRepository implements UserMasteryRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, UserMastery> byId = new LinkedHashMap<>();
    private final Map<Long, Map<Long, UserMastery>> byUserAndKp = new LinkedHashMap<>();

    @Override
    public synchronized UserMastery save(UserMastery mastery) {
        UserMastery stored = mastery.id() == 0L
                ? mastery.withId(idGenerator.getAndIncrement())
                : mastery;
        byId.put(stored.id(), stored);
        byUserAndKp
                .computeIfAbsent(stored.userId(), key -> new LinkedHashMap<>())
                .put(stored.knowledgePointId(), stored);
        return stored;
    }

    @Override
    public synchronized Optional<UserMastery> findByUserAndKp(long userId, long knowledgePointId) {
        Map<Long, UserMastery> userMap = byUserAndKp.get(userId);
        return userMap == null ? Optional.empty() : Optional.ofNullable(userMap.get(knowledgePointId));
    }

    @Override
    public synchronized List<UserMastery> listByUser(long userId) {
        Map<Long, UserMastery> userMap = byUserAndKp.get(userId);
        if (userMap == null) {
            return List.of();
        }
        return new ArrayList<>(userMap.values());
    }
}
