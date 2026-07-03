package com.onboarding.progress;

import java.util.List;
import java.util.Optional;

public interface UserMasteryRepository {

    /** Insert (assigns id when 0) or update an existing mastery row. */
    UserMastery save(UserMastery mastery);

    Optional<UserMastery> findByUserAndKp(long userId, long knowledgePointId);

    List<UserMastery> listByUser(long userId);
}
