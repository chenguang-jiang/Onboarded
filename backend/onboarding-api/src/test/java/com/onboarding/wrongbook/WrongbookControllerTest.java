package com.onboarding.wrongbook;

import com.onboarding.auth.AuthController;
import com.onboarding.auth.AuthService;
import com.onboarding.auth.MockWechatSessionClient;
import com.onboarding.auth.SimpleTokenService;
import com.onboarding.content.SeedContentRepository;
import com.onboarding.progress.InMemoryUserMasteryRepository;
import com.onboarding.progress.MasteryService;
import com.onboarding.question.AnswerService;
import com.onboarding.question.InMemoryAnswerRecordRepository;
import com.onboarding.question.QuestionController;
import com.onboarding.user.InMemoryUserAccountRepository;
import com.onboarding.user.InMemoryUserOnboardingStateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, QuestionController.class, WrongbookController.class})
@Import({
        AuthService.class,
        MockWechatSessionClient.class,
        SimpleTokenService.class,
        SeedContentRepository.class,
        AnswerService.class,
        WrongbookService.class,
        MasteryService.class,
        InMemoryAnswerRecordRepository.class,
        InMemoryWrongQuestionRepository.class,
        InMemoryUserMasteryRepository.class,
        InMemoryUserAccountRepository.class,
        InMemoryUserOnboardingStateRepository.class
})
class WrongbookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void correctSubmissionDoesNotCreateWrongQuestion() throws Exception {
        String token = loginAndGetToken("wb-correct");
        submitAnswer(token, 1, "A");

        mockMvc.perform(get("/api/wrongbook")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.pendingCount").value(0))
                .andExpect(jsonPath("$.data.masteredCount").value(0));
    }

    @Test
    void wrongSubmissionAppearsInWrongbook() throws Exception {
        String token = loginAndGetToken("wb-wrong");
        long wrongQuestionId = wrongQuestionIdOf(submitAnswer(token, 1, "B"));

        mockMvc.perform(get("/api/wrongbook")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].wrongCount").value(1))
                .andExpect(jsonPath("$.data.items[0].status").value("OPEN"))
                .andExpect(jsonPath("$.data.pendingCount").value(1))
                .andExpect(jsonPath("$.data.masteredCount").value(0));

        mockMvc.perform(get("/api/wrongbook/" + wrongQuestionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stem", not(emptyString())))
                .andExpect(jsonPath("$.data.options", hasSize(4)))
                .andExpect(jsonPath("$.data.answerKey").doesNotExist());
    }

    @Test
    void twoWrongSubmissionsIncrementWrongCount() throws Exception {
        String token = loginAndGetToken("wb-twice");
        submitAnswer(token, 1, "B");
        submitAnswer(token, 1, "B");

        mockMvc.perform(get("/api/wrongbook")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].wrongCount").value(2));
    }

    @Test
    void redoCorrectTwiceAutoMasters() throws Exception {
        String token = loginAndGetToken("wb-redo2");
        long wrongQuestionId = wrongQuestionIdOf(submitAnswer(token, 1, "B"));

        mockMvc.perform(post("/api/wrongbook/" + wrongQuestionId + "/redo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selectedAnswer": "A"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isCorrect").value(true))
                .andExpect(jsonPath("$.data.status").value("RETRYING"))
                .andExpect(jsonPath("$.data.autoMastered").value(false))
                .andExpect(jsonPath("$.data.mastered").value(false));

        mockMvc.perform(post("/api/wrongbook/" + wrongQuestionId + "/redo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selectedAnswer": "A"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isCorrect").value(true))
                .andExpect(jsonPath("$.data.status").value("MASTERED"))
                .andExpect(jsonPath("$.data.autoMastered").value(true))
                .andExpect(jsonPath("$.data.mastered").value(true));

        mockMvc.perform(get("/api/wrongbook")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.pendingCount").value(0))
                .andExpect(jsonPath("$.data.masteredCount").value(1));
    }

    @Test
    void redoWrongResetsConsecutiveCorrect() throws Exception {
        String token = loginAndGetToken("wb-reset");
        long wrongQuestionId = wrongQuestionIdOf(submitAnswer(token, 1, "B"));

        // First correct redo -> RETRYING (1/2).
        redo(token, wrongQuestionId, "A");

        // Wrong redo -> back to OPEN, consecutive reset to 0.
        redo(token, wrongQuestionId, "B");

        // Correct redo again -> RETRYING (1/2), not mastered.
        mockMvc.perform(post("/api/wrongbook/" + wrongQuestionId + "/redo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selectedAnswer": "A"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isCorrect").value(true))
                .andExpect(jsonPath("$.data.status").value("RETRYING"))
                .andExpect(jsonPath("$.data.autoMastered").value(false))
                .andExpect(jsonPath("$.data.mastered").value(false));
    }

    @Test
    void manualMasteredHidesFromList() throws Exception {
        String token = loginAndGetToken("wb-manual");
        long wrongQuestionId = wrongQuestionIdOf(submitAnswer(token, 1, "B"));

        mockMvc.perform(post("/api/wrongbook/" + wrongQuestionId + "/mastered")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("MASTERED"))
                .andExpect(jsonPath("$.data.mastered").value(true));

        mockMvc.perform(get("/api/wrongbook")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.masteredCount").value(1));
    }

    @Test
    void listRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/wrongbook"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void redoRejectsMissingToken() throws Exception {
        mockMvc.perform(post("/api/wrongbook/1/redo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selectedAnswer": "A"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private MvcResult submitAnswer(String token, long questionId, String answer) throws Exception {
        return mockMvc.perform(post("/api/questions/" + questionId + "/answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selectedAnswer": "%s"}
                                """.formatted(answer)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void redo(String token, long wrongQuestionId, String answer) throws Exception {
        mockMvc.perform(post("/api/wrongbook/" + wrongQuestionId + "/redo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selectedAnswer": "%s"}
                                """.formatted(answer)))
                .andExpect(status().isOk());
    }

    private long wrongQuestionIdOf(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/wrongQuestionId").asLong();
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
