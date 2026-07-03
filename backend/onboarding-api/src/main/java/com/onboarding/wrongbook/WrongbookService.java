package com.onboarding.wrongbook;

import com.onboarding.content.Chapter;
import com.onboarding.content.KnowledgePoint;
import com.onboarding.content.PracticeQuestion;
import com.onboarding.content.SeedContentRepository;
import com.onboarding.progress.MasteryService;
import com.onboarding.question.AnswerRecord;
import com.onboarding.question.AnswerRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WrongbookService {

    private static final int MASTERED_CONSECUTIVE_CORRECT_THRESHOLD = 2;

    private final WrongQuestionRepository wrongQuestionRepository;
    private final SeedContentRepository seedContentRepository;
    private final AnswerRecordRepository answerRecordRepository;
    private final MasteryService masteryService;

    public WrongbookService(
            WrongQuestionRepository wrongQuestionRepository,
            SeedContentRepository seedContentRepository,
            AnswerRecordRepository answerRecordRepository,
            MasteryService masteryService
    ) {
        this.wrongQuestionRepository = wrongQuestionRepository;
        this.seedContentRepository = seedContentRepository;
        this.answerRecordRepository = answerRecordRepository;
        this.masteryService = masteryService;
    }

    public WrongbookResponse listWrongbook(long userId) {
        List<WrongQuestion> all = wrongQuestionRepository.listByUser(userId);
        int masteredCount = (int) all.stream().filter(WrongQuestion::mastered).count();
        List<WrongQuestionListItem> items = all.stream()
                .filter(wq -> !wq.mastered())
                .sorted(Comparator.comparing(WrongQuestion::lastWrongAt).reversed())
                .map(this::toListItem)
                .toList();
        return new WrongbookResponse(items, items.size(), masteredCount);
    }

    public List<WrongbookChapterResponse> listChapters(long userId) {
        Map<Long, List<WrongQuestion>> byChapter = wrongQuestionRepository.listByUser(userId).stream()
                .filter(wq -> !wq.mastered())
                .collect(Collectors.groupingBy(WrongQuestion::chapterId));

        return byChapter.entrySet().stream()
                .map(entry -> {
                    long chapterId = entry.getKey();
                    String title = seedContentRepository.getChapter(chapterId).title();
                    return new WrongbookChapterResponse(chapterId, title, entry.getValue().size());
                })
                .sorted(Comparator.comparingLong(WrongbookChapterResponse::chapterId))
                .toList();
    }

    public WrongQuestionListItem getDetail(long userId, long wrongQuestionId) {
        return toListItem(requireOwned(userId, wrongQuestionId));
    }

    public RedoResponse redo(long userId, long wrongQuestionId, RedoRequest request) {
        WrongQuestion wq = requireOwned(userId, wrongQuestionId);
        PracticeQuestion question = seedContentRepository.getQuestionById(wq.questionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "question not found"));
        boolean isCorrect = request.selectedAnswer().equals(question.answerKey());
        Instant now = Instant.now();

        answerRecordRepository.save(new AnswerRecord(
                0L, userId, wq.questionId(), request.selectedAnswer(), isCorrect, null, null, now
        ));
        masteryService.recordRedo(userId, wq.knowledgePointId(), isCorrect);

        boolean autoMastered = false;
        if (isCorrect) {
            wq = wrongQuestionRepository.save(wq.recordCorrectRedo(now));
            if (wq.consecutiveCorrect() >= MASTERED_CONSECUTIVE_CORRECT_THRESHOLD) {
                wq = wrongQuestionRepository.save(wq.markMastered(now));
                autoMastered = true;
            }
        } else {
            wq = wrongQuestionRepository.save(wq.recordWrong(now));
        }

        return new RedoResponse(
                isCorrect,
                question.answerKey(),
                question.explanation(),
                wq.status(),
                autoMastered,
                wq.mastered()
        );
    }

    public MasteredResponse markMastered(long userId, long wrongQuestionId) {
        WrongQuestion wq = requireOwned(userId, wrongQuestionId);
        if (wq.mastered()) {
            return new MasteredResponse(wq.id(), wq.status(), true);
        }
        wq = wrongQuestionRepository.save(wq.markMastered(Instant.now()));
        return new MasteredResponse(wq.id(), wq.status(), wq.mastered());
    }

    private WrongQuestion requireOwned(long userId, long wrongQuestionId) {
        WrongQuestion wq = wrongQuestionRepository.findById(wrongQuestionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wrong question not found"));
        if (wq.userId() != userId) {
            // Don't leak existence to other users.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "wrong question not found");
        }
        return wq;
    }

    private WrongQuestionListItem toListItem(WrongQuestion wq) {
        PracticeQuestion question = seedContentRepository.getQuestionById(wq.questionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "question not found"));
        KnowledgePoint knowledgePoint = seedContentRepository.getKnowledgePointById(question.knowledgePointId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "knowledge point not found"));
        Chapter chapter = seedContentRepository.getChapter(knowledgePoint.chapterId());
        return new WrongQuestionListItem(
                wq.id(),
                question.id(),
                chapter.id(),
                chapter.title(),
                knowledgePoint.id(),
                knowledgePoint.title(),
                question.stem(),
                question.options(),
                wq.wrongCount(),
                wq.status(),
                wq.lastWrongAt() == null ? null : wq.lastWrongAt().toString()
        );
    }
}
