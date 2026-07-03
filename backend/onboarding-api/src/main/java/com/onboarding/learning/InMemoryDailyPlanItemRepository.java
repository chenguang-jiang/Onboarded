package com.onboarding.learning;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("standalone")
public class InMemoryDailyPlanItemRepository implements DailyPlanItemRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, DailyPlanItem> byId = new LinkedHashMap<>();
    private final Map<Long, List<DailyPlanItem>> byPlan = new LinkedHashMap<>();

    @Override
    public synchronized DailyPlanItem save(DailyPlanItem item) {
        DailyPlanItem stored = item.id() == 0L ? item.withId(idGenerator.getAndIncrement()) : item;
        byId.put(stored.id(), stored);
        List<DailyPlanItem> items = byPlan.computeIfAbsent(stored.planId(), key -> new ArrayList<>());
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id() == stored.id()) {
                items.set(i, stored);
                return stored;
            }
        }
        items.add(stored);
        return stored;
    }

    @Override
    public synchronized void saveAll(List<DailyPlanItem> items) {
        for (DailyPlanItem item : items) {
            save(item);
        }
    }

    @Override
    public synchronized List<DailyPlanItem> findByPlan(long planId) {
        List<DailyPlanItem> items = byPlan.get(planId);
        return items == null ? List.of() : new ArrayList<>(items);
    }

    @Override
    public synchronized List<DailyPlanItem> findByPlans(List<Long> planIds) {
        if (planIds.isEmpty()) {
            return List.of();
        }
        List<DailyPlanItem> result = new ArrayList<>();
        for (Long planId : planIds) {
            result.addAll(findByPlan(planId));
        }
        return result;
    }

    @Override
    public synchronized Optional<DailyPlanItem> findById(long itemId) {
        return Optional.ofNullable(byId.get(itemId));
    }
}
