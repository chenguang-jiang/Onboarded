package com.onboarding.ai;

import java.util.List;
import java.util.Optional;

public interface AiChatMessageRepository {

    AiChatMessage save(AiChatMessage message);

    Optional<AiChatMessage> findById(long id);

    List<AiChatMessage> findBySession(long sessionId);
}
