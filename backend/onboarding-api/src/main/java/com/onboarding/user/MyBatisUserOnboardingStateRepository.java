package com.onboarding.user;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class MyBatisUserOnboardingStateRepository implements UserOnboardingStateRepository {

    private final UserOnboardingStateMapper mapper;

    public MyBatisUserOnboardingStateRepository(UserOnboardingStateMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserOnboardingState findOrCreateByUserId(long userId) {
        UserOnboardingStateEntity entity = mapper.selectById(userId);
        if (entity == null) {
            entity = new UserOnboardingStateEntity();
            entity.setUserId(userId);
            entity.setCompleted(false);
            entity.setLastStep("WX_LOGIN");
            mapper.insert(entity);
        }
        return entity.toRecord();
    }

    @Override
    public UserOnboardingState markCompleted(long userId) {
        UserOnboardingStateEntity entity = mapper.selectById(userId);
        if (entity == null) {
            entity = new UserOnboardingStateEntity();
            entity.setUserId(userId);
            entity.setCompleted(true);
            entity.setLastStep("COMPLETED");
            mapper.insert(entity);
        } else {
            entity.setCompleted(true);
            entity.setLastStep("COMPLETED");
            mapper.updateById(entity);
        }
        return entity.toRecord();
    }
}
