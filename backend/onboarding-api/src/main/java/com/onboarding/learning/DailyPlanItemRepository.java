package com.onboarding.learning;

import java.util.List;
import java.util.Optional;

public interface DailyPlanItemRepository {

    DailyPlanItem save(DailyPlanItem item);

    void saveAll(List<DailyPlanItem> items);

    List<DailyPlanItem> findByPlan(long planId);

    List<DailyPlanItem> findByPlans(List<Long> planIds);

    Optional<DailyPlanItem> findById(long itemId);
}
