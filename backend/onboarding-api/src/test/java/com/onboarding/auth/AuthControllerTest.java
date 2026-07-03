package com.onboarding.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
        AuthService.class,
        MockWechatSessionClient.class,
        SimpleTokenService.class,
        com.onboarding.user.InMemoryUserAccountRepository.class,
        com.onboarding.user.InMemoryUserOnboardingStateRepository.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void wxLoginCreatesUserAndRequiresOnboardingForFirstLogin() throws Exception {
        mockMvc.perform(post("/api/auth/wx-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "dev-code-001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.openid").value("mock-openid-dev-code-001"))
                .andExpect(jsonPath("$.data.token", not("dev-token-1")))
                .andExpect(jsonPath("$.data.onboardingRequired").value(true));
    }

    @Test
    void wxLoginRejectsBlankCode() throws Exception {
        mockMvc.perform(post("/api/auth/wx-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": " "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
