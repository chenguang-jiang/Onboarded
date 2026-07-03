package com.onboarding.question;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class MyBatisAnswerRecordRepository implements AnswerRecordRepository {

    private final AnswerRecordMapper mapper;

    public MyBatisAnswerRecordRepository(AnswerRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AnswerRecord save(AnswerRecord record) {
        AnswerRecordEntity entity = AnswerRecordEntity.from(record);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return entity.toRecord();
    }
}
