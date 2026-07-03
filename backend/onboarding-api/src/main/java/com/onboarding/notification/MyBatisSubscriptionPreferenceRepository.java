package com.onboarding.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("local")
public class MyBatisSubscriptionPreferenceRepository implements SubscriptionPreferenceRepository {

    private final SubscriptionPreferenceMapper mapper;

    public MyBatisSubscriptionPreferenceRepository(SubscriptionPreferenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SubscriptionPreference save(SubscriptionPreference preference) {
        SubscriptionPreferenceEntity entity = SubscriptionPreferenceEntity.from(preference);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return entity.toRecord();
    }

    @Override
    public Optional<SubscriptionPreference> findByUserAndScene(long userId, String scene) {
        SubscriptionPreferenceEntity entity = mapper.selectOne(
                new LambdaQueryWrapper<SubscriptionPreferenceEntity>()
                        .eq(SubscriptionPreferenceEntity::getUserId, userId)
                        .eq(SubscriptionPreferenceEntity::getScene, scene));
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }
}
