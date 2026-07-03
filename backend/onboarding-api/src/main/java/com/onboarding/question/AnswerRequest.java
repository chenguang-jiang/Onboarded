package com.onboarding.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerRequest(
        @NotBlank(message = "selectedAnswer is required")
        @Size(max = 8, message = "selectedAnswer must be a single option key")
        String selectedAnswer,

        Integer durationSec
) {
}
