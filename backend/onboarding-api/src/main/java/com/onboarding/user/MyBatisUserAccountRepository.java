package com.onboarding.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Repository
@Profile("local")
public class MyBatisUserAccountRepository implements UserAccountRepository {

    private final UserAccountMapper mapper;

    public MyBatisUserAccountRepository(UserAccountMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserAccount findOrCreateByOpenid(String openid, String unionid) {
        UserAccountEntity existing = mapper.selectOne(
                new LambdaQueryWrapper<UserAccountEntity>().eq(UserAccountEntity::getOpenid, openid));
        if (existing != null) {
            return existing.toRecord();
        }
        UserAccountEntity created = new UserAccountEntity();
        created.setOpenid(openid);
        created.setUnionid(unionid);
        created.setDailyTarget(15);
        mapper.insert(created);
        return created.toRecord();
    }

    @Override
    public Optional<UserAccount> findById(long userId) {
        UserAccountEntity entity = mapper.selectById(userId);
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }

    @Override
    public UserAccount updateStudySettings(long userId, LocalDate examDate, int dailyTarget, LocalTime reminderTime) {
        UserAccountEntity entity = mapper.selectById(userId);
        if (entity == null) {
            throw new IllegalArgumentException("user not found");
        }
        entity.setExamDate(examDate);
        entity.setDailyTarget(dailyTarget);
        entity.setReminderTime(reminderTime);
        mapper.updateById(entity);
        return entity.toRecord();
    }
}
