package com.onboarding.ai;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealGlmClientTest {

    @Test
    void buildRequestBodyUsesGlm52RetrievalToolAndKnowledgeId() {
        GlmProperties properties = configuredProperties();
        RealGlmClient client = new RealGlmClient(properties, RestClient.builder());

        Map<String, Object> body = client.buildRequestBody("质量属性场景怎么写？");

        assertEquals("glm-5.2", body.get("model"));
        assertEquals(false, body.get("stream"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        assertEquals("user", messages.getFirst().get("role"));
        assertEquals("质量属性场景怎么写？", messages.getFirst().get("content"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tools = (List<Map<String, Object>>) body.get("tools");
        assertEquals("retrieval", tools.getFirst().get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> retrieval = (Map<String, Object>) tools.getFirst().get("retrieval");
        assertEquals("2072256899017056256", retrieval.get("knowledge_id"));
        assertTrue(retrieval.get("prompt_template").toString().contains("{{knowledge}}"));
        assertTrue(retrieval.get("prompt_template").toString().contains("{{question}}"));
    }

    @Test
    void askRejectsMissingApiKeyBeforeHttpCall() {
        GlmProperties properties = configuredProperties();
        properties.setApiKey("");
        RealGlmClient client = new RealGlmClient(properties, RestClient.builder());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> client.ask("x"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertTrue(ex.getReason().contains("GLM_API_KEY"));
    }

    @Test
    void askRejectsMissingKnowledgeIdBeforeHttpCall() {
        GlmProperties properties = configuredProperties();
        properties.setKnowledgeId("");
        RealGlmClient client = new RealGlmClient(properties, RestClient.builder());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> client.ask("x"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertTrue(ex.getReason().contains("GLM_KNOWLEDGE_ID"));
    }

    private static GlmProperties configuredProperties() {
        GlmProperties properties = new GlmProperties();
        properties.setApiKey("test-key");
        properties.setModel("glm-5.2");
        properties.setKnowledgeId("2072256899017056256");
        properties.setTimeoutSeconds(60);
        properties.setMock(false);
        return properties;
    }
}
