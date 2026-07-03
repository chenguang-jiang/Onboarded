package com.onboarding.question;

import com.onboarding.auth.AuthController;
import com.onboarding.auth.AuthService;
import com.onboarding.auth.MockWechatSessionClient;
import com.onboarding.auth.SimpleTokenService;
import com.onboarding.content.SeedContentRepository;
import com.onboarding.progress.InMemoryUserMasteryRepository;
import com.onboarding.progress.MasteryService;
import com.onboarding.user.InMemoryUserAccountRepository;
import com.onboarding.user.InMemoryUserOnboardingStateRepository;
import com.onboarding.wrongbook.InMemoryWrongQuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, QuestionController.class})
@Import({
        AuthService.class,
        MockWechatSessionClient.class,
        SimpleTokenService.class,
        SeedContentRepository.class,
        AnswerService.class,
        MasteryService.class,
        InMemoryAnswerRecordRepository.class,
        InMemoryWrongQuestionRepository.class,
        InMemoryUserMasteryRepository.class,
        InMemoryUserAccountRepository.class,
        InMemoryUserOnboardingStateRepository.class
})
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getQuestionReturnsDetailWithoutAnswerKey() throws Exception {
        String token = loginAndGetToken("dev-code-question-detail");

        mockMvc.perform(get("/api/questions/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.stem", not(emptyString())))
                .andExpect(jsonPath("$.data.options", org.hamcrest.Matchers.hasSize(4)))
                .andExpect(jsonPath("$.data.answerKey").doesNotExist())
                .andExpect(jsonPath("$.data.explanation").doesNotExist());
    }

    @Test
    void submitCorrectAnswerReturnsTrueAndNoWrongQuestion() throws Exception {
        String token = loginAndGetToken("dev-code-question-correct");

        mockMvc.perform(post("/api/questions/1/answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selectedAnswer": "A"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isCorrect").value(true))
                .andExpect(jsonPath("$.data.correctAnswer").value("A"))
                .andExpect(jsonPath("$.data.explanation", not(emptyString())))
                .andExpect(jsonPath("$.data.status").isEmpty())
                .andExpect(jsonPath("$.data.wrongQuestionId").isEmpty());
    }

    @Test
    void submitWrongAnswerCreatesOpenWrongQuestion() throws Exception {
        String token = loginAndGetToken("dev-code-question-wrong");

        mockMvc.perform(post("/api/questions/1/answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selectedAnswer": "B"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isCorrect").value(false))
                .andExpect(jsonPath("$.data.correctAnswer").value("A"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.wrongQuestionId").isNumber());
    }

    @Test
    void submitAnswerRejectsMissingToken() throws Exception {
        mockMvc.perform(post("/api/questions/1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selectedAnswer": "A"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void submitAnswerRejectsMissingSelectedAnswer() throws Exception {
        String token = loginAndGetToken("dev-code-question-invalid");

        mockMvc.perform(post("/api/questions/1/answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getQuestionReturns404ForUnknownId() throws Exception {
        String token = loginAndGetToken("dev-code-question-404");

        mockMvc.perform(get("/api/questions/9999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private String loginAndGetToken(String code) throws Exception {
        String response = mockMvc.perform(post("/api/auth/wx-login")
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
