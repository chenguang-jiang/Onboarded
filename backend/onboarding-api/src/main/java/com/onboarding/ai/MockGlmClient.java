package com.onboarding.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Default GLM client used when {@code glm.mock=true} (the default). Returns a structured canned
 * answer so the AI flow is fully runnable without an API key. Switch off with {@code GLM_MOCK=false}.
 */
@Component
@ConditionalOnProperty(prefix = "glm", name = "mock", havingValue = "true", matchIfMissing = true)
public class MockGlmClient implements GlmClient {

    @Override
    public GlmAnswer ask(String userQuestion) {
        String topic = userQuestion == null ? "" : userQuestion.trim();
        String content = """
                【结论】关于「%s」，这是软考系统架构师备考的核心知识点之一。
                【解释】建议结合官方教材与真题，理解其定义、适用场景与常见陷阱。
                【记忆点】抓住关键词，并关联相邻知识点形成记忆链。
                【易错提醒】注意与相似概念的区分，避免在选择题中混淆。
                参考来源：软考系统架构师知识库（mock）。""".formatted(topic);
        return new GlmAnswer(
                content,
                List.of("软考系统架构师知识库 / 相关章节"),
                128,
                true
        );
    }
}
