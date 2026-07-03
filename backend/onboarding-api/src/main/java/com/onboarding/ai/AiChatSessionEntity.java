package com.onboarding.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

@TableName("ai_chat_session")
public class AiChatSessionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;

    public AiChatSessionEntity() {
    }

    public static AiChatSessionEntity from(AiChatSession s) {
        AiChatSessionEntity e = new AiChatSessionEntity();
        e.id = s.id() == 0L ? null : s.id();
        e.userId = s.userId();
        e.title = s.title();
        e.createdAt = s.createdAt();
        e.updatedAt = s.updatedAt();
        return e;
    }

    public AiChatSession toRecord() {
        return new AiChatSession(id == null ? 0L : id, userId, title, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
