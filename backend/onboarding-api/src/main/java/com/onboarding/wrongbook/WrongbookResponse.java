package com.onboarding.wrongbook;

import java.util.List;

public record WrongbookResponse(
        List<WrongQuestionListItem> items,
        int pendingCount,
        int masteredCount
) {
}
