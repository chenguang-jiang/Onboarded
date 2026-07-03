package com.onboarding.learning;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("local")
public class MyBatisDailyPlanRepository implements DailyPlanRepository {

    private final DailyPlanMapper mapper;

    public MyBatisDailyPlanRepository(DailyPlanMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public DailyPlan save(DailyPlan plan) {
        DailyPlanEntity entity = DailyPlanEntity.from(plan);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return entity.toRecord();
    }

    @Override
    public Optional<DailyPlan> findByUserAndDate(long userId, LocalDate planDate) {
        DailyPlanEntity entity = mapper.selectOne(
                new LambdaQueryWrapper<DailyPlanEntity>()
                        .eq(DailyPlanEntity::getUserId, userId)
                        .eq(DailyPlanEntity::getPlanDate, planDate));
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }

    @Override
    public List<DailyPlan> findBeforeDate(long userId, LocalDate beforeDate) {
        return mapper.selectList(
                        new LambdaQueryWrapper<DailyPlanEntity>()
                                .eq(DailyPlanEntity::getUserId, userId)
                                .lt(DailyPlanEntity::getPlanDate, beforeDate))
                .stream()
                .map(DailyPlanEntity::toRecord)
                .sorted(Comparator.comparing(DailyPlan::planDate).reversed().thenComparing(DailyPlan::id))
                .toList();
    }

    @Override
    public Optional<DailyPlan> findById(long planId) {
        DailyPlanEntity entity = mapper.selectById(planId);
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }
}
