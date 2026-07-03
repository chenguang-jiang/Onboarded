package com.onboarding.wrongbook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedoRequest(
        @NotBlank(message = "selectedAnswer is required")
        @Size(max = 8, message = "selectedAnswer must be a single option key")
        String selectedAnswer
) {
}
