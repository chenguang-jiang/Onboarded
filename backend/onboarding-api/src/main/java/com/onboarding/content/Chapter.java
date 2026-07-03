package com.onboarding.content;

public record Chapter(
        long id,
        String title,
        String description,
        int sortOrder
) {
}
