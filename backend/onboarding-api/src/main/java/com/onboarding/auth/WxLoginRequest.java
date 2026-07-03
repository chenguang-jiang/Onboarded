package com.onboarding.auth;

import jakarta.validation.constraints.NotBlank;

public record WxLoginRequest(
        @NotBlank(message = "code is required")
        String code
) {
}
