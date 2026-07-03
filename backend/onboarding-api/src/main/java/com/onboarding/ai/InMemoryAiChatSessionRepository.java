package com.onboarding.ai;

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
public class InMemoryAiChatSessionRepository implements AiChatSessionRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, AiChatSession> byId = new LinkedHashMap<>();
    private final Map<Long, List<AiChatSession>> byUser = new LinkedHashMap<>();

    @Override
    public synchronized AiChatSession save(AiChatSession session) {
        AiChatSession stored = session.id() == 0L ? session.withId(idGenerator.getAndIncrement()) : session;
        byId.put(stored.id(), stored);
        byUser.computeIfAbsent(stored.userId(), key -> new ArrayList<>()).replaceAll(s -> s.id() == stored.id() ? stored : s);
        List<AiChatSession> userSessions = byUser.get(stored.userId());
        if (userSessions.stream().noneMatch(s -> s.id() == stored.id())) {
            userSessions.add(stored);
        }
        return stored;
    }

    @Override
    public synchronized Optional<AiChatSession> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public synchronized List<AiChatSession> listByUser(long userId) {
        List<AiChatSession> sessions = byUser.get(userId);
        return sessions == null ? List.of() : new ArrayList<>(sessions);
    }
}
