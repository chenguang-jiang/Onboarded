package com.onboarding.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskRequest(
        @NotBlank(message = "question is required")
        @Size(max = 500, message = "question must be at most 500 characters")
        String question
) {
}
