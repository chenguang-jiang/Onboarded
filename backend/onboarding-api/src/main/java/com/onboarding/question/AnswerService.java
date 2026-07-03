package com.onboarding.question;

import com.onboarding.content.KnowledgePoint;
import com.onboarding.content.PracticeQuestion;
import com.onboarding.content.SeedContentRepository;
import com.onboarding.progress.MasteryService;
import com.onboarding.wrongbook.WrongQuestion;
import com.onboarding.wrongbook.WrongQuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class AnswerService {

    private final SeedContentRepository seedContentRepository;
    private final AnswerRecordRepository answerRecordRepository;
    private final WrongQuestionRepository wrongQuestionRepository;
    private final MasteryService masteryService;

    public AnswerService(
            SeedContentRepository seedContentRepository,
            AnswerRecordRepository answerRecordRepository,
            WrongQuestionRepository wrongQuestionRepository,
            MasteryService masteryService
    ) {
        this.seedContentRepository = seedContentRepository;
        this.answerRecordRepository = answerRecordRepository;
        this.wrongQuestionRepository = wrongQuestionRepository;
        this.masteryService = masteryService;
    }

    public QuestionDetailResponse getQuestion(long questionId) {
        PracticeQuestion question = requireQuestion(questionId);
        return new QuestionDetailResponse(
                question.id(),
                question.knowledgePointId(),
                question.stem(),
                question.options()
        );
    }

    public AnswerResponse submitAnswer(long userId, long questionId, AnswerRequest request) {
        PracticeQuestion question = requireQuestion(questionId);
        KnowledgePoint knowledgePoint = seedContentRepository
                .getKnowledgePointById(question.knowledgePointId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "knowledge point not found"));

        boolean isCorrect = request.selectedAnswer().equals(question.answerKey());
        Instant now = Instant.now();

        answerRecordRepository.save(new AnswerRecord(
                0L, userId, questionId, request.selectedAnswer(), isCorrect, request.durationSec(), null, now
        ));
        masteryService.recordPracticeAnswer(userId, knowledgePoint.id(), isCorrect);

        if (isCorrect) {
            return new AnswerResponse(
                    true,
                    question.answerKey(),
                    question.explanation(),
                    null,
                    null,
                    "回答正确，继续下一个知识点。"
            );
        }

        WrongQuestion wrongQuestion = wrongQuestionRepository
                .findByUserAndQuestion(userId, questionId)
                .map(existing -> wrongQuestionRepository.save(existing.recordWrong(now)))
                .orElseGet(() -> wrongQuestionRepository.save(WrongQuestion.newOpen(
                        userId, questionId, knowledgePoint.chapterId(), knowledgePoint.id(), now
                )));

        return new AnswerResponse(
                false,
                question.answerKey(),
                question.explanation(),
                wrongQuestion.id(),
                wrongQuestion.status(),
                "回答错误，已加入错题本，可在错题本重做。"
        );
    }

    private PracticeQuestion requireQuestion(long questionId) {
        return seedContentRepository
                .getQuestionById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "question not found"));
    }
}
