package com.onboarding.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * GLM configuration (docs/02-backend-design.md §7.1). Mirrors the WeChat mock/real switch:
 * {@code glm.mock=true} (default) uses {@link MockGlmClient} so the app runs without an API key;
 * {@code glm.mock=false} uses {@link RealGlmClient} and requires {@code GLM_API_KEY}/{@code GLM_KNOWLEDGE_ID}.
 */
@Component
@ConfigurationProperties(prefix = "glm")
public class GlmProperties {

    private String apiKey;
    private String model = "glm-5.2";
    private String knowledgeId;
    private int timeoutSeconds = 60;
    private int maxDailyRequestsPerUser = 100;
    private boolean mock = true;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getKnowledgeId() {
        return knowledgeId;
    }

    public void setKnowledgeId(String knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getMaxDailyRequestsPerUser() {
        return maxDailyRequestsPerUser;
    }

    public void setMaxDailyRequestsPerUser(int maxDailyRequestsPerUser) {
        this.maxDailyRequestsPerUser = maxDailyRequestsPerUser;
    }

    public boolean isMock() {
        return mock;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
    }
}
