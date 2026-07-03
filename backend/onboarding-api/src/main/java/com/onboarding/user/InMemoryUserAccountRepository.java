package com.onboarding.user;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("standalone")
public class InMemoryUserAccountRepository implements UserAccountRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<String, UserAccount> usersByOpenid = new LinkedHashMap<>();
    private final Map<Long, UserAccount> usersById = new LinkedHashMap<>();

    @Override
    public synchronized UserAccount findOrCreateByOpenid(String openid, String unionid) {
        UserAccount existing = usersByOpenid.get(openid);
        if (existing != null) {
            return existing;
        }

        UserAccount created = new UserAccount(idGenerator.getAndIncrement(), openid, unionid, null, 15, null);
        usersByOpenid.put(openid, created);
        usersById.put(created.id(), created);
        return created;
    }

    @Override
    public synchronized Optional<UserAccount> findById(long userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    @Override
    public synchronized UserAccount updateStudySettings(
            long userId,
            LocalDate examDate,
            int dailyTarget,
            LocalTime reminderTime
    ) {
        UserAccount user = usersById.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        UserAccount updated = user.withStudySettings(examDate, dailyTarget, reminderTime);
        usersById.put(userId, updated);
        usersByOpenid.put(updated.openid(), updated);
        return updated;
    }
}
