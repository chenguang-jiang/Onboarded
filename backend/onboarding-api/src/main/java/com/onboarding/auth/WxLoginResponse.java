package com.onboarding.auth;

public record WxLoginResponse(
        long userId,
        String openid,
        String token,
        boolean onboardingRequired
) {
}
