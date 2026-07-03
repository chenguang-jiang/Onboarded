package com.onboarding.auth;

import com.onboarding.user.UserAccount;
import com.onboarding.user.UserAccountRepository;
import com.onboarding.user.UserOnboardingState;
import com.onboarding.user.UserOnboardingStateRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final WechatSessionClient wechatSessionClient;
    private final UserAccountRepository userAccountRepository;
    private final UserOnboardingStateRepository onboardingStateRepository;
    private final SimpleTokenService tokenService;

    public AuthService(
            WechatSessionClient wechatSessionClient,
            UserAccountRepository userAccountRepository,
            UserOnboardingStateRepository onboardingStateRepository,
            SimpleTokenService tokenService
    ) {
        this.wechatSessionClient = wechatSessionClient;
        this.userAccountRepository = userAccountRepository;
        this.onboardingStateRepository = onboardingStateRepository;
        this.tokenService = tokenService;
    }

    public WxLoginResponse wxLogin(WxLoginRequest request) {
        WechatSession session = wechatSessionClient.exchangeCode(request.code());
        UserAccount user = userAccountRepository.findOrCreateByOpenid(session.openid(), session.unionid());
        UserOnboardingState onboardingState = onboardingStateRepository.findOrCreateByUserId(user.id());

        return new WxLoginResponse(
                user.id(),
                user.openid(),
                tokenService.issueToken(user.id()),
                !onboardingState.completed()
        );
    }
}
