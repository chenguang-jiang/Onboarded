package com.onboarding.learning;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("local")
public class MyBatisDailyPlanItemRepository implements DailyPlanItemRepository {

    private final DailyPlanItemMapper mapper;

    public MyBatisDailyPlanItemRepository(DailyPlanItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public DailyPlanItem save(DailyPlanItem item) {
        DailyPlanItemEntity entity = DailyPlanItemEntity.from(item);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return entity.toRecord();
    }

    @Override
    public void saveAll(List<DailyPlanItem> items) {
        for (DailyPlanItem item : items) {
            save(item);
        }
    }

    @Override
    public List<DailyPlanItem> findByPlan(long planId) {
        return mapper.selectList(
                new LambdaQueryWrapper<DailyPlanItemEntity>().eq(DailyPlanItemEntity::getPlanId, planId))
                .stream()
                .map(DailyPlanItemEntity::toRecord)
                .toList();
    }

    @Override
    public List<DailyPlanItem> findByPlans(List<Long> planIds) {
        if (planIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectList(
                        new LambdaQueryWrapper<DailyPlanItemEntity>().in(DailyPlanItemEntity::getPlanId, planIds))
                .stream()
                .map(DailyPlanItemEntity::toRecord)
                .toList();
    }

    @Override
    public Optional<DailyPlanItem> findById(long itemId) {
        DailyPlanItemEntity entity = mapper.selectById(itemId);
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }
}
