package com.onboarding.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.time.Instant;
import java.util.List;

@TableName(value = "ai_review_item", autoResultMap = true)
public class AiReviewItemEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long messageId;
    private String content;
    @TableField(value = "references_json", typeHandler = JacksonTypeHandler.class)
    private List<String> references;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static AiReviewItemEntity from(AiReviewItem item) {
        AiReviewItemEntity e = new AiReviewItemEntity();
        e.id = item.id() == 0L ? null : item.id();
        e.userId = item.userId();
        e.messageId = item.messageId();
        e.content = item.content();
        e.references = item.references();
        e.status = item.status();
        e.createdAt = item.createdAt();
        e.updatedAt = item.updatedAt();
        return e;
    }

    public AiReviewItem toRecord() {
        return new AiReviewItem(
                id == null ? 0L : id,
                userId == null ? 0L : userId,
                messageId == null ? 0L : messageId,
                content,
                references == null ? List.of() : references,
                status,
                createdAt,
                updatedAt
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getReferences() {
        return references;
    }

    public void setReferences(List<String> references) {
        this.references = references;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
