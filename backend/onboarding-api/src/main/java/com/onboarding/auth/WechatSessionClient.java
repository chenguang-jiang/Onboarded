package com.onboarding.auth;

public interface WechatSessionClient {

    WechatSession exchangeCode(String code);
}
