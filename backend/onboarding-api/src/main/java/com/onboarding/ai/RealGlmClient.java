package com.onboarding.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Real GLM-5.2 client (docs/02 §7.2). Calls the ZhipuAI chat-completions endpoint with the
 * retrieval tool bound to the configured knowledge base. Only active when {@code glm.mock=false},
 * which requires {@code GLM_API_KEY} and {@code GLM_KNOWLEDGE_ID}. Like the real WeChat client,
 * this is a scaffold that is not exercised by the local test suite.
 */
@Component
@ConditionalOnProperty(prefix = "glm", name = "mock", havingValue = "false")
public class RealGlmClient implements GlmClient {

    static final String API_BASE_URL = "https://open.bigmodel.cn";
    static final String COMPLETIONS_PATH = "/api/paas/v4/chat/completions";

    private static final String PROMPT_TEMPLATE = """
            你是软考系统架构师备考教练。优先根据知识库回答。若知识库没有依据，必须说明未找到明确资料。
            回答格式：结论、解释、考试记忆点、易错提醒、参考来源。知识库内容：{{knowledge}}。用户问题：{{question}}。""";

    private final GlmProperties properties;
    private final RestClient restClient;

    public RealGlmClient(GlmProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));
        this.restClient = restClientBuilder
                .baseUrl(API_BASE_URL)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public GlmAnswer ask(String userQuestion) {
        ensureConfigured();
        Map<String, Object> body = buildRequestBody(userQuestion);
        try {
            Map<String, Object> response = restClient.post()
                    .uri(COMPLETIONS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI service returned empty");
            }
            String content = extractContent(response);
            int tokens = extractTotalTokens(response);
            return new GlmAnswer(content, List.of("GLM knowledge base: " + properties.getKnowledgeId()), tokens, true);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI service unavailable", ex);
        }
    }

    Map<String, Object> buildRequestBody(String userQuestion) {
        return Map.of(
                "model", properties.getModel(),
                "messages", List.of(Map.of("role", "user", "content", userQuestion)),
                "tools", List.of(Map.of(
                        "type", "retrieval",
                        "retrieval", Map.of(
                                "knowledge_id", properties.getKnowledgeId(),
                                "prompt_template", PROMPT_TEMPLATE
                        )
                )),
                "stream", false
        );
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "GLM_API_KEY 未配置");
        }
        if (!StringUtils.hasText(properties.getKnowledgeId())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "GLM_KNOWLEDGE_ID 未配置");
        }
        if (!StringUtils.hasText(properties.getModel())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "GLM model 未配置");
        }
    }

    @SuppressWarnings("unchecked")
    private static String extractContent(Map<String, Object> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        Object content = message == null ? null : message.get("content");
        return content == null ? "" : content.toString();
    }

    @SuppressWarnings("unchecked")
    private static int extractTotalTokens(Map<String, Object> response) {
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        Object total = usage == null ? null : usage.get("total_tokens");
        return total instanceof Number n ? n.intValue() : 0;
    }
}
