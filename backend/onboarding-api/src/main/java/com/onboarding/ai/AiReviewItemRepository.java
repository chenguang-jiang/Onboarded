package com.onboarding.ai;

import java.util.Optional;

public interface AiReviewItemRepository {

    AiReviewItem save(AiReviewItem item);

    Optional<AiReviewItem> findByUserAndMessage(long userId, long messageId);
}
