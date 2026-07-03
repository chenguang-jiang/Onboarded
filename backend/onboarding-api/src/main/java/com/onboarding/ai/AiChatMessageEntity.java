package com.onboarding.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.time.Instant;
import java.util.List;

@TableName(value = "ai_chat_message", autoResultMap = true)
public class AiChatMessageEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long userId;
    private String role;
    private String content;
    @TableField(value = "references_json", typeHandler = JacksonTypeHandler.class)
    private List<String> references;
    private Integer tokensUsed;
    private Instant createdAt;

    public AiChatMessageEntity() {
    }

    public static AiChatMessageEntity from(AiChatMessage m) {
        AiChatMessageEntity e = new AiChatMessageEntity();
        e.id = m.id() == 0L ? null : m.id();
        e.sessionId = m.sessionId();
        e.userId = m.userId();
        e.role = m.role();
        e.content = m.content();
        e.references = m.references();
        e.tokensUsed = m.tokensUsed();
        e.createdAt = m.createdAt();
        return e;
    }

    public AiChatMessage toRecord() {
        return new AiChatMessage(
                id == null ? 0L : id, sessionId, userId, role, content,
                references == null ? List.of() : references,
                tokensUsed == null ? 0 : tokensUsed, createdAt
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getReferences() { return references; }
    public void setReferences(List<String> references) { this.references = references; }
    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
