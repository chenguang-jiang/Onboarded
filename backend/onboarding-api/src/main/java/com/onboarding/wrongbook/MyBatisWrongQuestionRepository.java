package com.onboarding.wrongbook;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis-Plus backed {@link WrongQuestionRepository}, active only under the {@code local} profile
 * (MySQL). The {@code standalone} profile uses {@link InMemoryWrongQuestionRepository}.
 */
@Repository
@Profile("local")
public class MyBatisWrongQuestionRepository implements WrongQuestionRepository {

    private final WrongQuestionMapper mapper;

    public MyBatisWrongQuestionRepository(WrongQuestionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public WrongQuestion save(WrongQuestion wrongQuestion) {
        WrongQuestionEntity entity = WrongQuestionEntity.from(wrongQuestion);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return entity.toRecord();
    }

    @Override
    public Optional<WrongQuestion> findById(long id) {
        WrongQuestionEntity entity = mapper.selectById(id);
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }

    @Override
    public Optional<WrongQuestion> findByUserAndQuestion(long userId, long questionId) {
        WrongQuestionEntity entity = mapper.selectOne(
                new LambdaQueryWrapper<WrongQuestionEntity>()
                        .eq(WrongQuestionEntity::getUserId, userId)
                        .eq(WrongQuestionEntity::getQuestionId, questionId));
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }

    @Override
    public List<WrongQuestion> listByUser(long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<WrongQuestionEntity>()
                        .eq(WrongQuestionEntity::getUserId, userId))
                .stream()
                .map(WrongQuestionEntity::toRecord)
                .toList();
    }
}
