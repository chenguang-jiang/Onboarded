package com.onboarding.user;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface UserAccountRepository {

    UserAccount findOrCreateByOpenid(String openid, String unionid);

    Optional<UserAccount> findById(long userId);

    UserAccount updateStudySettings(long userId, LocalDate examDate, int dailyTarget, LocalTime reminderTime);
}
