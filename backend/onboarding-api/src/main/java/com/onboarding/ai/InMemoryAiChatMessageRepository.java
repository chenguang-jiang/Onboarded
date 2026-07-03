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
public class InMemoryAiChatMessageRepository implements AiChatMessageRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, AiChatMessage> byId = new LinkedHashMap<>();
    private final Map<Long, List<AiChatMessage>> bySession = new LinkedHashMap<>();

    @Override
    public synchronized AiChatMessage save(AiChatMessage message) {
        AiChatMessage stored = message.id() == 0L ? message.withId(idGenerator.getAndIncrement()) : message;
        byId.put(stored.id(), stored);
        List<AiChatMessage> messages = bySession.computeIfAbsent(stored.sessionId(), key -> new ArrayList<>());
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).id() == stored.id()) {
                messages.set(i, stored);
                return stored;
            }
        }
        messages.add(stored);
        return stored;
    }

    @Override
    public synchronized Optional<AiChatMessage> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public synchronized List<AiChatMessage> findBySession(long sessionId) {
        List<AiChatMessage> messages = bySession.get(sessionId);
        return messages == null ? List.of() : new ArrayList<>(messages);
    }
}
