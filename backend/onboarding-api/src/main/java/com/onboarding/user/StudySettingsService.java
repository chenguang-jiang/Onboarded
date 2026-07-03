package com.onboarding.user;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class StudySettingsService {

    private static final DateTimeFormatter REMINDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final UserAccountRepository userAccountRepository;
    private final UserOnboardingStateRepository onboardingStateRepository;

    public StudySettingsService(
            UserAccountRepository userAccountRepository,
            UserOnboardingStateRepository onboardingStateRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.onboardingStateRepository = onboardingStateRepository;
    }

    public StudySettingsResponse updateStudySettings(long userId, StudySettingsRequest request) {
        LocalTime reminderTime = LocalTime.parse(request.reminderTime(), REMINDER_TIME_FORMATTER);
        UserAccount updated = userAccountRepository.updateStudySettings(
                userId,
                request.examDate(),
                request.dailyTarget(),
                reminderTime
        );
        UserOnboardingState onboardingState = onboardingStateRepository.markCompleted(userId);

        return new StudySettingsResponse(
                updated.examDate(),
                updated.dailyTarget(),
                updated.reminderTime().format(REMINDER_TIME_FORMATTER),
                onboardingState.completed()
        );
    }
}
