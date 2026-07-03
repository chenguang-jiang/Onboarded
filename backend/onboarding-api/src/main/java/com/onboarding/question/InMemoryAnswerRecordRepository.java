package com.onboarding.question;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("standalone")
public class InMemoryAnswerRecordRepository implements AnswerRecordRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final List<AnswerRecord> records = new ArrayList<>();

    @Override
    public synchronized AnswerRecord save(AnswerRecord record) {
        AnswerRecord stored = record.withId(idGenerator.getAndIncrement());
        records.add(stored);
        return stored;
    }
}
