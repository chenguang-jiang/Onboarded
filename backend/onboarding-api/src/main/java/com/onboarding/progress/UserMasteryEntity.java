package com.onboarding.progress;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

@TableName("user_mastery")
public class UserMasteryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long knowledgePointId;
    private Integer masteryScore;
    private Integer studyCount;
    private Integer correctCount;
    private Integer wrongCount;
    private Instant lastReviewAt;
    private Instant nextReviewAt;
    private Instant createdAt;
    private Instant updatedAt;

    public UserMasteryEntity() {
    }

    public static UserMasteryEntity from(UserMastery m) {
        UserMasteryEntity e = new UserMasteryEntity();
        e.id = m.id() == 0L ? null : m.id();
        e.userId = m.userId();
        e.knowledgePointId = m.knowledgePointId();
        e.masteryScore = m.masteryScore();
        e.studyCount = m.studyCount();
        e.correctCount = m.correctCount();
        e.wrongCount = m.wrongCount();
        e.lastReviewAt = m.lastReviewAt();
        e.nextReviewAt = m.nextReviewAt();
        e.createdAt = m.createdAt();
        e.updatedAt = m.updatedAt();
        return e;
    }

    public UserMastery toRecord() {
        return new UserMastery(
                id == null ? 0L : id, userId, knowledgePointId,
                masteryScore == null ? 0 : masteryScore,
                studyCount == null ? 0 : studyCount,
                correctCount == null ? 0 : correctCount,
                wrongCount == null ? 0 : wrongCount,
                lastReviewAt, nextReviewAt, createdAt, updatedAt
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getKnowledgePointId() { return knowledgePointId; }
    public void setKnowledgePointId(Long knowledgePointId) { this.knowledgePointId = knowledgePointId; }
    public Integer getMasteryScore() { return masteryScore; }
    public void setMasteryScore(Integer masteryScore) { this.masteryScore = masteryScore; }
    public Integer getStudyCount() { return studyCount; }
    public void setStudyCount(Integer studyCount) { this.studyCount = studyCount; }
    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }
    public Integer getWrongCount() { return wrongCount; }
    public void setWrongCount(Integer wrongCount) { this.wrongCount = wrongCount; }
    public Instant getLastReviewAt() { return lastReviewAt; }
    public void setLastReviewAt(Instant lastReviewAt) { this.lastReviewAt = lastReviewAt; }
    public Instant getNextReviewAt() { return nextReviewAt; }
    public void setNextReviewAt(Instant nextReviewAt) { this.nextReviewAt = nextReviewAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
