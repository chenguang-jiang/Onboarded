package com.onboarding.user;

import com.onboarding.auth.AuthController;
import com.onboarding.auth.AuthService;
import com.onboarding.auth.MockWechatSessionClient;
import com.onboarding.auth.SimpleTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AuthController.class,
        StudySettingsController.class
})
@Import({
        AuthService.class,
        MockWechatSessionClient.class,
        SimpleTokenService.class,
        StudySettingsService.class,
        InMemoryUserAccountRepository.class,
        InMemoryUserOnboardingStateRepository.class
})
class StudySettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void updateStudySettingsCompletesOnboarding() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(put("/api/users/me/study-settings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "examDate": "2026-11-08",
                                  "dailyTarget": 15,
                                  "reminderTime": "08:30"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.examDate").value("2026-11-08"))
                .andExpect(jsonPath("$.data.dailyTarget").value(15))
                .andExpect(jsonPath("$.data.reminderTime").value("08:30"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true));
    }

    @Test
    void updateStudySettingsRejectsMissingToken() throws Exception {
        mockMvc.perform(put("/api/users/me/study-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "examDate": "2026-11-08",
                                  "dailyTarget": 15,
                                  "reminderTime": "08:30"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateStudySettingsRejectsInvalidDailyTarget() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(put("/api/users/me/study-settings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "examDate": "2026-11-08",
                                  "dailyTarget": 0,
                                  "reminderTime": "08:30"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private String loginAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/wx-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "dev-code-settings"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        int tokenStart = response.indexOf("\"token\":\"") + 9;
        int tokenEnd = response.indexOf("\"", tokenStart);
        return response.substring(tokenStart, tokenEnd);
    }
}
