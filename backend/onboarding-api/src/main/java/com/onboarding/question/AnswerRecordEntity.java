package com.onboarding.question;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

@TableName("answer_record")
public class AnswerRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long questionId;
    private String selectedAnswer;
    private Boolean isCorrect;
    private Integer durationSec;
    private String errorReason;
    private Instant createdAt;

    public AnswerRecordEntity() {
    }

    public static AnswerRecordEntity from(AnswerRecord r) {
        AnswerRecordEntity e = new AnswerRecordEntity();
        e.id = r.id() == 0L ? null : r.id();
        e.userId = r.userId();
        e.questionId = r.questionId();
        e.selectedAnswer = r.selectedAnswer();
        e.isCorrect = r.isCorrect();
        e.durationSec = r.durationSec();
        e.errorReason = r.errorReason();
        e.createdAt = r.createdAt();
        return e;
    }

    public AnswerRecord toRecord() {
        return new AnswerRecord(
                id == null ? 0L : id, userId, questionId, selectedAnswer,
                Boolean.TRUE.equals(isCorrect), durationSec, errorReason, createdAt
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getSelectedAnswer() { return selectedAnswer; }
    public void setSelectedAnswer(String selectedAnswer) { this.selectedAnswer = selectedAnswer; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
    public String getErrorReason() { return errorReason; }
    public void setErrorReason(String errorReason) { this.errorReason = errorReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
