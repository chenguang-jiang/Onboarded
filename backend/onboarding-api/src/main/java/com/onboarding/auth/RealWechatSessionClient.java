package com.onboarding.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
@ConditionalOnProperty(prefix = "wechat.mini-program", name = "mock", havingValue = "false")
public class RealWechatSessionClient implements WechatSessionClient {

    private static final String CODE2_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final RestClient restClient;
    private final WechatMiniProgramProperties properties;

    public RealWechatSessionClient(RestClient.Builder restClientBuilder, WechatMiniProgramProperties properties) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

    @Override
    public WechatSession exchangeCode(String code) {
        ensureConfigured();
        Code2SessionResponse response = restClient.get()
                .uri(CODE2_SESSION_URL, uriBuilder -> uriBuilder
                        .queryParam("appid", properties.getAppId())
                        .queryParam("secret", properties.getAppSecret())
                        .queryParam("js_code", code)
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .retrieve()
                .body(Code2SessionResponse.class);

        if (response == null || !StringUtils.hasText(response.openid())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "微信登录换取 openid 失败");
        }
        if (response.errcode() != null && response.errcode() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, response.errmsg());
        }
        return new WechatSession(response.openid(), response.unionid());
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getAppSecret())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "微信小程序 AppID/AppSecret 未配置");
        }
    }

    private record Code2SessionResponse(
            String openid,
            String session_key,
            String unionid,
            Integer errcode,
            String errmsg
    ) {
    }
}
