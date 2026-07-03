package com.onboarding.auth;

public record WechatSession(
        String openid,
        String unionid
) {
}
