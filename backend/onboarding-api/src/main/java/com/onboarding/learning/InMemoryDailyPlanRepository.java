package com.onboarding.learning;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("standalone")
public class InMemoryDailyPlanRepository implements DailyPlanRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, DailyPlan> byId = new LinkedHashMap<>();
    private final Map<String, DailyPlan> byUserAndDate = new LinkedHashMap<>();

    @Override
    public synchronized DailyPlan save(DailyPlan plan) {
        DailyPlan stored = plan.id() == 0L ? plan.withId(idGenerator.getAndIncrement()) : plan;
        byId.put(stored.id(), stored);
        byUserAndDate.put(key(stored.userId(), stored.planDate()), stored);
        return stored;
    }

    @Override
    public synchronized Optional<DailyPlan> findByUserAndDate(long userId, LocalDate planDate) {
        return Optional.ofNullable(byUserAndDate.get(key(userId, planDate)));
    }

    @Override
    public synchronized List<DailyPlan> findBeforeDate(long userId, LocalDate beforeDate) {
        return byId.values().stream()
                .filter(plan -> plan.userId() == userId)
                .filter(plan -> plan.planDate().isBefore(beforeDate))
                .sorted(Comparator.comparing(DailyPlan::planDate).reversed().thenComparing(DailyPlan::id))
                .toList();
    }

    @Override
    public synchronized Optional<DailyPlan> findById(long planId) {
        return Optional.ofNullable(byId.get(planId));
    }

    private static String key(long userId, LocalDate date) {
        return userId + "|" + date;
    }
}
