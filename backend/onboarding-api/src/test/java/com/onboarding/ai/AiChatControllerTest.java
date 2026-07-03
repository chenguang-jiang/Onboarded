package com.onboarding.ai;

import com.onboarding.auth.AuthController;
import com.onboarding.auth.AuthService;
import com.onboarding.auth.MockWechatSessionClient;
import com.onboarding.auth.SimpleTokenService;
import com.onboarding.user.InMemoryUserAccountRepository;
import com.onboarding.user.InMemoryUserOnboardingStateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, AiChatController.class})
@Import({
        AuthService.class,
        MockWechatSessionClient.class,
        SimpleTokenService.class,
        AiChatService.class,
        MockGlmClient.class,
        AiRateLimiter.class,
        GlmProperties.class,
        InMemoryAiChatSessionRepository.class,
        InMemoryAiChatMessageRepository.class,
        InMemoryAiReviewItemRepository.class,
        InMemoryUserAccountRepository.class,
        InMemoryUserOnboardingStateRepository.class
})
class AiChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createSessionReturnsSession() throws Exception {
        String token = loginAndGetToken("ai-create");
        mockMvc.perform(post("/api/ai/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"架构风格答疑"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("架构风格答疑"))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void askReturnsAssistantMessageAndPersistsThread() throws Exception {
        String token = loginAndGetToken("ai-ask");
        long sessionId = sessionIdOf(createSession(token, "质量属性"));

        MvcResult askResult = mockMvc.perform(post("/api/ai/sessions/" + sessionId + "/ask")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"质量属性场景怎么写？"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("assistant"))
                .andExpect(jsonPath("$.data.content", org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString())))
                .andExpect(jsonPath("$.data.references", hasSize(1)))
                .andReturn();

        mockMvc.perform(get("/api/ai/sessions/" + sessionId + "/messages")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].role").value("user"))
                .andExpect(jsonPath("$.data[1].role").value("assistant"));
    }

    @Test
    void assistantMessageCanBeAddedToReviewQueue() throws Exception {
        String token = loginAndGetToken("ai-review");
        long sessionId = sessionIdOf(createSession(token, "错题解释"));

        long assistantMessageId = messageIdOf(mockMvc.perform(post("/api/ai/sessions/" + sessionId + "/ask")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"流水线吞吐率为什么容易考错？"}
                                """))
                .andExpect(status().isOk())
                .andReturn());

        mockMvc.perform(post("/api/ai/messages/" + assistantMessageId + "/review-items")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageId").value(assistantMessageId))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void reviewQueueRejectsForeignMessage() throws Exception {
        String ownerToken = loginAndGetToken("ai-review-owner");
        long sessionId = sessionIdOf(createSession(ownerToken, "private-ai"));

        long assistantMessageId = messageIdOf(mockMvc.perform(post("/api/ai/sessions/" + sessionId + "/ask")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"ATAM 怎么答？"}
                                """))
                .andExpect(status().isOk())
                .andReturn());

        String otherToken = loginAndGetToken("ai-review-other");
        mockMvc.perform(post("/api/ai/messages/" + assistantMessageId + "/review-items")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void askRejectsBlankQuestion() throws Exception {
        String token = loginAndGetToken("ai-blank");
        long sessionId = sessionIdOf(createSession(token, null));
        mockMvc.perform(post("/api/ai/sessions/" + sessionId + "/ask")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void askRejectsForeignSession() throws Exception {
        String ownerToken = loginAndGetToken("ai-owner");
        long sessionId = sessionIdOf(createSession(ownerToken, "private"));
        String otherToken = loginAndGetToken("ai-other");
        mockMvc.perform(post("/api/ai/sessions/" + sessionId + "/ask")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"想看别人的会话"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSessionRejectsMissingToken() throws Exception {
        mockMvc.perform(post("/api/ai/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"x"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void askRejectsMissingToken() throws Exception {
        mockMvc.perform(post("/api/ai/sessions/1/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"x"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private MvcResult createSession(String token, String title) throws Exception {
        String body = title == null ? "{}" : "{\"title\":\"%s\"}".formatted(title);
        return mockMvc.perform(post("/api/ai/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
    }

    private long sessionIdOf(MvcResult result) throws Exception {
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return data.get("id").asLong();
    }

    private long messageIdOf(MvcResult result) throws Exception {
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        return data.get("id").asLong();
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
        int start = response.indexOf("\"token\":\"") + 9;
        return response.substring(start, response.indexOf("\"", start));
    }
}
