package com.onboarding.ai;

import java.util.List;
import java.util.Optional;

public interface AiChatSessionRepository {

    AiChatSession save(AiChatSession session);

    Optional<AiChatSession> findById(long id);

    List<AiChatSession> listByUser(long userId);
}
