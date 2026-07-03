package com.onboarding.wrongbook;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

/**
 * MyBatis-Plus persistence entity for {@code wrong_question} (active only under the {@code local}
 * profile). Mirrors the {@link WrongQuestion} record; {@link #from(WrongQuestion)} / {@link #toRecord()}
 * bridge between the immutable domain record and this mutable POJO.
 */
@TableName("wrong_question")
public class WrongQuestionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long questionId;
    private Long chapterId;
    private Long knowledgePointId;
    private Integer wrongCount;
    private Boolean mastered;
    private Integer consecutiveCorrect;
    private String status;
    private Instant lastWrongAt;
    private Instant masteredAt;
    private Instant createdAt;
    private Instant updatedAt;

    public WrongQuestionEntity() {
    }

    public static WrongQuestionEntity from(WrongQuestion wq) {
        WrongQuestionEntity e = new WrongQuestionEntity();
        e.id = wq.id() == 0L ? null : wq.id();
        e.userId = wq.userId();
        e.questionId = wq.questionId();
        e.chapterId = wq.chapterId();
        e.knowledgePointId = wq.knowledgePointId();
        e.wrongCount = wq.wrongCount();
        e.mastered = wq.mastered();
        e.consecutiveCorrect = wq.consecutiveCorrect();
        e.status = wq.status();
        e.lastWrongAt = wq.lastWrongAt();
        e.masteredAt = wq.masteredAt();
        e.createdAt = wq.createdAt();
        e.updatedAt = wq.updatedAt();
        return e;
    }

    public WrongQuestion toRecord() {
        return new WrongQuestion(
                id == null ? 0L : id,
                userId == null ? 0L : userId,
                questionId == null ? 0L : questionId,
                chapterId == null ? 0L : chapterId,
                knowledgePointId == null ? 0L : knowledgePointId,
                wrongCount == null ? 0 : wrongCount,
                Boolean.TRUE.equals(mastered),
                consecutiveCorrect == null ? 0 : consecutiveCorrect,
                status,
                lastWrongAt,
                masteredAt,
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

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Long getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(Long knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(Integer wrongCount) {
        this.wrongCount = wrongCount;
    }

    public Boolean getMastered() {
        return mastered;
    }

    public void setMastered(Boolean mastered) {
        this.mastered = mastered;
    }

    public Integer getConsecutiveCorrect() {
        return consecutiveCorrect;
    }

    public void setConsecutiveCorrect(Integer consecutiveCorrect) {
        this.consecutiveCorrect = consecutiveCorrect;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getLastWrongAt() {
        return lastWrongAt;
    }

    public void setLastWrongAt(Instant lastWrongAt) {
        this.lastWrongAt = lastWrongAt;
    }

    public Instant getMasteredAt() {
        return masteredAt;
    }

    public void setMasteredAt(Instant masteredAt) {
        this.masteredAt = masteredAt;
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
