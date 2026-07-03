package com.onboarding.wrongbook;

import java.util.List;
import java.util.Optional;

public interface WrongQuestionRepository {

    /** Insert (assigns id when 0) or update an existing wrong question. */
    WrongQuestion save(WrongQuestion wrongQuestion);

    Optional<WrongQuestion> findById(long id);

    Optional<WrongQuestion> findByUserAndQuestion(long userId, long questionId);

    List<WrongQuestion> listByUser(long userId);
}
