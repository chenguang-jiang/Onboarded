package com.onboarding.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "wechat.mini-program", name = "mock", havingValue = "true", matchIfMissing = true)
public class MockWechatSessionClient implements WechatSessionClient {

    @Override
    public WechatSession exchangeCode(String code) {
        return new WechatSession("mock-openid-" + code, null);
    }
}
