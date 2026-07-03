package com.onboarding.ai;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-user daily request cap (docs/02 §7.1 {@code max-daily-requests-per-user}, §7.3 限流).
 * In-memory; resets when the calendar day changes. Returns false once the daily quota is exhausted.
 */
@Component
public class AiRateLimiter {

    private final GlmProperties properties;
    private final Map<Long, DailyCount> countsByUser = new LinkedHashMap<>();

    public AiRateLimiter(GlmProperties properties) {
        this.properties = properties;
    }

    public synchronized boolean tryAcquire(long userId) {
        LocalDate today = LocalDate.now();
        DailyCount count = countsByUser.get(userId);
        if (count == null || !today.equals(count.date())) {
            count = new DailyCount(today, 0);
            countsByUser.put(userId, count);
        }
        if (count.count() >= properties.getMaxDailyRequestsPerUser()) {
            return false;
        }
        count.increment();
        return true;
    }

    private static final class DailyCount {
        private final LocalDate date;
        private int count;

        private DailyCount(LocalDate date, int count) {
            this.date = date;
            this.count = count;
        }

        LocalDate date() {
            return date;
        }

        int count() {
            return count;
        }

        void increment() {
            count++;
        }
    }
}
