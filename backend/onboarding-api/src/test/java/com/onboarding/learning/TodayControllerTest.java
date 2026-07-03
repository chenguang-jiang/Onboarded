package com.onboarding.learning;

import com.onboarding.auth.AuthController;
import com.onboarding.auth.AuthService;
import com.onboarding.auth.MockWechatSessionClient;
import com.onboarding.auth.SimpleTokenService;
import com.onboarding.content.SeedContentRepository;
import com.onboarding.question.AnswerService;
import com.onboarding.question.InMemoryAnswerRecordRepository;
import com.onboarding.question.QuestionController;
import com.onboarding.progress.InMemoryUserMasteryRepository;
import com.onboarding.progress.MasteryService;
import com.onboarding.user.InMemoryUserAccountRepository;
import com.onboarding.user.InMemoryUserOnboardingStateRepository;
import com.onboarding.wrongbook.InMemoryWrongQuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, QuestionController.class, TodayController.class})
@Import({
        AuthService.class,
        MockWechatSessionClient.class,
        SimpleTokenService.class,
        SeedContentRepository.class,
        AnswerService.class,
        MasteryService.class,
        DailyPlanService.class,
        InMemoryAnswerRecordRepository.class,
        InMemoryWrongQuestionRepository.class,
        InMemoryUserMasteryRepository.class,
        InMemoryDailyPlanRepository.class,
        InMemoryDailyPlanItemRepository.class,
        InMemoryUserAccountRepository.class,
        InMemoryUserOnboardingStateRepository.class
})
class TodayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DailyPlanRepository dailyPlanRepository;

    @Autowired
    private DailyPlanItemRepository dailyPlanItemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void freshUserTodayGeneratesFifteenPendingItems() throws Exception {
        String token = loginAndGetToken("today-fresh");
        mockMvc.perform(get("/api/today").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(15))
                .andExpect(jsonPath("$.data.completedCount").value(0))
                .andExpect(jsonPath("$.data.items", hasSize(15)))
                .andExpect(jsonPath("$.data.items[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.items[0].source").value("NEW"))
                .andExpect(jsonPath("$.data.items[0].knowledge.id").value(1))
                .andExpect(jsonPath("$.data.items[0].question.answerKey").doesNotExist());
    }

    @Test
    void getTodayIsIdempotent() throws Exception {
        String token = loginAndGetToken("today-idem");
        long first = planIdOf(fetchToday(token));
        long second = planIdOf(fetchToday(token));
        assertEquals(first, second);
    }

    @Test
    void startItemMarksStudying() throws Exception {
        String token = loginAndGetToken("today-start");
        long itemId = firstItemId(fetchToday(token));
        mockMvc.perform(post("/api/today/items/" + itemId + "/start").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].status").value("STUDYING"))
                .andExpect(jsonPath("$.data.completedCount").value(0));
    }

    @Test
    void completeItemMarksDoneAndIncrementsCount() throws Exception {
        String token = loginAndGetToken("today-complete");
        long itemId = firstItemId(fetchToday(token));
        mockMvc.perform(post("/api/today/items/" + itemId + "/complete").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].status").value("DONE"))
                .andExpect(jsonPath("$.data.completedCount").value(1));
        // idempotent: completing again does not bump the count
        mockMvc.perform(post("/api/today/items/" + itemId + "/complete").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].status").value("DONE"))
                .andExpect(jsonPath("$.data.completedCount").value(1));
    }

    @Test
    void wrongAnswerUserSurfacesWeakKpFirst() throws Exception {
        String token = loginAndGetToken("today-weak");
        submitAnswer(token, 1, "B"); // kp1 wrong -> wrong_question + low mastery
        mockMvc.perform(get("/api/today").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].knowledge.id").value(1))
                .andExpect(jsonPath("$.data.items[0].source").value("WRONG_RELATED"));
    }

    @Test
    void unfinishedHistoricalItemsCarryOverFirstAndAvoidDuplicates() throws Exception {
        LoginResult login = login("today-carry");
        String token = login.token();
        long userId = login.userId();
        Instant now = Instant.now();
        DailyPlan yesterday = dailyPlanRepository.save(DailyPlan.newPlan(userId, LocalDate.now().minusDays(1), 2, now));
        dailyPlanItemRepository.save(DailyPlanItem.newItem(yesterday.id(), userId, 1L, 1L, DailyPlanItem.SOURCE_NEW, 0, now)
                .markComplete(now));
        dailyPlanItemRepository.save(DailyPlanItem.newItem(yesterday.id(), userId, 2L, 2L, DailyPlanItem.SOURCE_NEW, 1, now));

        mockMvc.perform(get("/api/today").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(15))
                .andExpect(jsonPath("$.data.items", hasSize(15)))
                .andExpect(jsonPath("$.data.items[0].knowledge.id").value(2))
                .andExpect(jsonPath("$.data.items[0].source").value("CARRY_OVER"));
    }

    @Test
    void getTodayRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/today")).andExpect(status().isUnauthorized());
    }

    @Test
    void getTodayRejectsTokenForMissingUser() throws Exception {
        mockMvc.perform(get("/api/today").header("Authorization", "Bearer dev-token-9999"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void completeRejectsMissingToken() throws Exception {
        mockMvc.perform(post("/api/today/items/1/complete")).andExpect(status().isUnauthorized());
    }

    private MvcResult fetchToday(String token) throws Exception {
        return mockMvc.perform(get("/api/today").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
    }

    private long planIdOf(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).at("/data/planId").asLong();
    }

    private long firstItemId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).at("/data/items/0/itemId").asLong();
    }

    private void submitAnswer(String token, long questionId, String answer) throws Exception {
        mockMvc.perform(post("/api/questions/" + questionId + "/answer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selectedAnswer": "%s"}
                                """.formatted(answer)))
                .andExpect(status().isOk());
    }

    private String loginAndGetToken(String code) throws Exception {
        return login(code).token();
    }

    private LoginResult login(String code) throws Exception {
        String response = mockMvc.perform(post("/api/auth/wx-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "%s"}
                                """.formatted(code)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long userId = objectMapper.readTree(response).at("/data/userId").asLong();
        String token = objectMapper.readTree(response).at("/data/token").asText();
        return new LoginResult(userId, token);
    }

    private record LoginResult(long userId, String token) {
    }
}
