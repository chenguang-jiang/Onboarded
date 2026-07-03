package com.onboarding.learning;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyPlanRepository {

    DailyPlan save(DailyPlan plan);

    Optional<DailyPlan> findByUserAndDate(long userId, LocalDate planDate);

    List<DailyPlan> findBeforeDate(long userId, LocalDate beforeDate);

    Optional<DailyPlan> findById(long planId);
}
