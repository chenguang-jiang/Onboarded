package com.onboarding.progress;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("local")
public class MyBatisUserMasteryRepository implements UserMasteryRepository {

    private final UserMasteryMapper mapper;

    public MyBatisUserMasteryRepository(UserMasteryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserMastery save(UserMastery mastery) {
        UserMasteryEntity entity = UserMasteryEntity.from(mastery);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return entity.toRecord();
    }

    @Override
    public Optional<UserMastery> findByUserAndKp(long userId, long knowledgePointId) {
        UserMasteryEntity entity = mapper.selectOne(
                new LambdaQueryWrapper<UserMasteryEntity>()
                        .eq(UserMasteryEntity::getUserId, userId)
                        .eq(UserMasteryEntity::getKnowledgePointId, knowledgePointId));
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }

    @Override
    public List<UserMastery> listByUser(long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<UserMasteryEntity>().eq(UserMasteryEntity::getUserId, userId))
                .stream()
                .map(UserMasteryEntity::toRecord)
                .toList();
    }
}
