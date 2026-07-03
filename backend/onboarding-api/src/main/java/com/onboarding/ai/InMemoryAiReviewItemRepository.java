package com.onboarding.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("standalone")
public class InMemoryAiReviewItemRepository implements AiReviewItemRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, AiReviewItem> byId = new LinkedHashMap<>();

    @Override
    public synchronized AiReviewItem save(AiReviewItem item) {
        AiReviewItem stored = item.id() == 0L ? item.withId(idGenerator.getAndIncrement()) : item;
        byId.put(stored.id(), stored);
        return stored;
    }

    @Override
    public synchronized Optional<AiReviewItem> findByUserAndMessage(long userId, long messageId) {
        return byId.values().stream()
                .filter(item -> item.userId() == userId && item.messageId() == messageId)
                .findFirst();
    }
}
