package com.onboarding.progress;

import com.onboarding.auth.AuthController;
import com.onboarding.auth.AuthService;
import com.onboarding.auth.MockWechatSessionClient;
import com.onboarding.auth.SimpleTokenService;
import com.onboarding.content.SeedContentRepository;
import com.onboarding.question.AnswerService;
import com.onboarding.question.InMemoryAnswerRecordRepository;
import com.onboarding.question.QuestionController;
import com.onboarding.user.InMemoryUserAccountRepository;
import com.onboarding.user.InMemoryUserOnboardingStateRepository;
import com.onboarding.wrongbook.InMemoryWrongQuestionRepository;
import com.onboarding.wrongbook.WrongbookController;
import com.onboarding.wrongbook.WrongbookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, QuestionController.class, WrongbookController.class, ProgressController.class})
@Import({
        AuthService.class,
        MockWechatSessionClient.class,
        SimpleTokenService.class,
        SeedContentRepository.class,
        AnswerService.class,
        MasteryService.class,
        WrongbookService.class,
        InMemoryAnswerRecordRepository.class,
        InMemoryWrongQuestionRepository.class,
        InMemoryUserMasteryRepository.class,
        InMemoryUserAccountRepository.class,
        InMemoryUserOnboardingStateRepository.class
})
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void freshUserShowsEmptyProgress() throws Exception {
        String token = loginAndGetToken("prog-fresh");

        mockMvc.perform(get("/api/progress/overview").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalKnowledgePoints").value(15))
                .andExpect(jsonPath("$.data.studiedCount").value(0))
                .andExpect(jsonPath("$.data.masteredCount").value(0))
                .andExpect(jsonPath("$.data.weakCount").value(0))
                .andExpect(jsonPath("$.data.averageScore").value(0));

        mockMvc.perform(get("/api/progress/chapters").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(5)));
    }

    @Test
    void correctAnswerShowsStudiedAndWeak() throws Exception {
        String token = loginAndGetToken("prog-correct");
        submitAnswer(token, 1, "A"); // kp1: +15 -> score 15

        mockMvc.perform(get("/api/progress/overview").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studiedCount").value(1))
                .andExpect(jsonPath("$.data.masteredCount").value(0))
                .andExpect(jsonPath("$.data.weakCount").value(1))
                .andExpect(jsonPath("$.data.averageScore").value(1)); // 15/15

        JsonNode ch1 = chapterById(token, 1);
        assertNotNull(ch1);
        assertEquals(1, ch1.get("studiedCount").asInt());
        assertEquals(5, ch1.get("averageScore").asInt()); // 15/3
        assertTrue(ch1.get("weak").asBoolean());

        JsonNode ch2 = chapterById(token, 2);
        assertNotNull(ch2);
        assertEquals(0, ch2.get("studiedCount").asInt());
        assertFalse(ch2.get("weak").asBoolean());
    }

    @Test
    void wrongAnswerClampsScoreToZero() throws Exception {
        String token = loginAndGetToken("prog-wrong");
        submitAnswer(token, 1, "B"); // first wrong: -15 -> clamp 0

        mockMvc.perform(get("/api/progress/overview").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studiedCount").value(1))
                .andExpect(jsonPath("$.data.masteredCount").value(0))
                .andExpect(jsonPath("$.data.weakCount").value(1))
                .andExpect(jsonPath("$.data.averageScore").value(0));

        JsonNode ch1 = chapterById(token, 1);
        assertNotNull(ch1);
        assertEquals(1, ch1.get("weakCount").asInt());
        assertEquals(0, ch1.get("averageScore").asInt());
        assertTrue(ch1.get("weak").asBoolean());
    }

    @Test
    void fiveCorrectAnswersMasterKnowledgePoint() throws Exception {
        String token = loginAndGetToken("prog-mastered");
        for (int i = 0; i < 5; i++) {
            submitAnswer(token, 1, "A"); // 5 * +15 = 75
        }

        mockMvc.perform(get("/api/progress/overview").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studiedCount").value(1))
                .andExpect(jsonPath("$.data.masteredCount").value(1)) // 75 >= 70
                .andExpect(jsonPath("$.data.weakCount").value(0))    // 75 >= 60
                .andExpect(jsonPath("$.data.averageScore").value(5)); // 75/15
    }

    @Test
    void redoCorrectRaisesChapterScore() throws Exception {
        String token = loginAndGetToken("prog-redo-ok");
        long wrongQuestionId = wrongQuestionIdOf(submitAnswer(token, 1, "B")); // score 0
        redo(token, wrongQuestionId, "A"); // +20 -> score 20

        JsonNode ch1 = chapterById(token, 1);
        assertNotNull(ch1);
        assertEquals(7, ch1.get("averageScore").asInt()); // 20/3 = 6.67 -> 7
        assertEquals(1, ch1.get("studiedCount").asInt());
    }

    @Test
    void redoWrongLowersChapterScore() throws Exception {
        String token = loginAndGetToken("prog-redo-wrong");
        long wrongQuestionId = wrongQuestionIdOf(submitAnswer(token, 1, "B")); // score 0
        redo(token, wrongQuestionId, "A"); // +20 -> score 20 (chapter avg 7)
        redo(token, wrongQuestionId, "B"); // -10 -> score 10

        JsonNode ch1 = chapterById(token, 1);
        assertNotNull(ch1);
        assertEquals(3, ch1.get("averageScore").asInt()); // 10/3 = 3.33 -> 3
    }

    @Test
    void overviewRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/progress/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chaptersRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/progress/chapters"))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode chapterById(String token, long chapterId) throws Exception {
        String body = mockMvc.perform(get("/api/progress/chapters").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode data = objectMapper.readTree(body).get("data");
        for (JsonNode node : data) {
            if (node.get("chapterId").asLong() == chapterId) {
                return node;
            }
        }
        return null;
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
