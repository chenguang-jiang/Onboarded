package com.onboarding.notification;

import com.onboarding.auth.AuthController;
import com.onboarding.auth.AuthService;
import com.onboarding.auth.MockWechatSessionClient;
import com.onboarding.auth.SimpleTokenService;
import com.onboarding.user.InMemoryUserAccountRepository;
import com.onboarding.user.InMemoryUserOnboardingStateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, NotificationController.class})
@Import({
        AuthService.class,
        MockWechatSessionClient.class,
        SimpleTokenService.class,
        NotificationService.class,
        InMemorySubscriptionPreferenceRepository.class,
        InMemoryUserAccountRepository.class,
        InMemoryUserOnboardingStateRepository.class
})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void saveAcceptedDailySubscription() throws Exception {
        String token = loginAndGetToken("notify-accepted");

        mockMvc.perform(put("/api/notifications/subscription")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateId": "daily-template",
                                  "scene": "DAILY_TASK",
                                  "accepted": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scene").value("DAILY_TASK"))
                .andExpect(jsonPath("$.data.accepted").value(true))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    void saveRejectedSubscriptionKeepsFallbackState() throws Exception {
        String token = loginAndGetToken("notify-rejected");

        mockMvc.perform(put("/api/notifications/subscription")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateId": "daily-template",
                                  "scene": "DAILY_TASK",
                                  "accepted": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accepted").value(false))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    private String loginAndGetToken(String code) throws Exception {
        String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/wx-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "%s"}
                                """.formatted(code)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int tokenStart = response.indexOf("\"token\":\"") + 9;
        int tokenEnd = response.indexOf("\"", tokenStart);
        return response.substring(tokenStart, tokenEnd);
    }
}
