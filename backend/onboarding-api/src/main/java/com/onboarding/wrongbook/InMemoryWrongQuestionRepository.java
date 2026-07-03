package com.onboarding.wrongbook;

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
public class InMemoryWrongQuestionRepository implements WrongQuestionRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, WrongQuestion> byId = new LinkedHashMap<>();
    private final Map<Long, Map<Long, WrongQuestion>> byUserAndQuestion = new LinkedHashMap<>();

    @Override
    public synchronized WrongQuestion save(WrongQuestion wrongQuestion) {
        WrongQuestion stored = wrongQuestion.id() == 0L
                ? wrongQuestion.withId(idGenerator.getAndIncrement())
                : wrongQuestion;
        byId.put(stored.id(), stored);
        byUserAndQuestion
                .computeIfAbsent(stored.userId(), key -> new LinkedHashMap<>())
                .put(stored.questionId(), stored);
        return stored;
    }

    @Override
    public synchronized Optional<WrongQuestion> findById(long id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public synchronized Optional<WrongQuestion> findByUserAndQuestion(long userId, long questionId) {
        Map<Long, WrongQuestion> userQuestions = byUserAndQuestion.get(userId);
        return userQuestions == null ? Optional.empty() : Optional.ofNullable(userQuestions.get(questionId));
    }

    @Override
    public synchronized List<WrongQuestion> listByUser(long userId) {
        Map<Long, WrongQuestion> userQuestions = byUserAndQuestion.get(userId);
        if (userQuestions == null) {
            return List.of();
        }
        return new ArrayList<>(userQuestions.values());
    }
}
