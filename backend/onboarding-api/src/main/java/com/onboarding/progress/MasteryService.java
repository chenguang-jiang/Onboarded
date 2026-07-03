package com.onboarding.progress;

import com.onboarding.content.Chapter;
import com.onboarding.content.KnowledgePoint;
import com.onboarding.content.SeedContentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MasteryService {

    private final UserMasteryRepository userMasteryRepository;
    private final SeedContentRepository seedContentRepository;

    public MasteryService(UserMasteryRepository userMasteryRepository, SeedContentRepository seedContentRepository) {
        this.userMasteryRepository = userMasteryRepository;
        this.seedContentRepository = seedContentRepository;
    }

    public void recordPracticeAnswer(long userId, long knowledgePointId, boolean isCorrect) {
        Instant now = Instant.now();
        UserMastery existing = userMasteryRepository.findByUserAndKp(userId, knowledgePointId)
                .orElseGet(() -> UserMastery.newMastery(userId, knowledgePointId, now));
        UserMastery updated = isCorrect
                ? existing.applyPracticeCorrect(now)
                : existing.applyPracticeWrong(now);
        userMasteryRepository.save(updated);
    }

    public void recordRedo(long userId, long knowledgePointId, boolean isCorrect) {
        Instant now = Instant.now();
        UserMastery existing = userMasteryRepository.findByUserAndKp(userId, knowledgePointId)
                .orElseGet(() -> UserMastery.newMastery(userId, knowledgePointId, now));
        UserMastery updated = isCorrect
                ? existing.applyRedoCorrect(now)
                : existing.applyRedoWrong(now);
        userMasteryRepository.save(updated);
    }

    public ProgressOverviewResponse overview(long userId) {
        Map<Long, UserMastery> byKp = masteryByKp(userId);
        List<KnowledgePoint> knowledgePoints = seedContentRepository.listKnowledgePoints();
        int total = knowledgePoints.size();
        int studied = 0;
        int mastered = 0;
        int weak = 0;
        int sum = 0;
        for (KnowledgePoint kp : knowledgePoints) {
            UserMastery mastery = byKp.get(kp.id());
            int score = mastery == null ? 0 : mastery.masteryScore();
            boolean studiedKp = mastery != null && mastery.studyCount() > 0;
            if (studiedKp) {
                studied++;
            }
            if (score >= 70) {
                mastered++;
            }
            if (studiedKp && score < 60) {
                weak++;
            }
            sum += score;
        }
        int average = total == 0 ? 0 : Math.round((float) sum / total);
        return new ProgressOverviewResponse(total, studied, mastered, weak, average);
    }

    public List<ChapterProgressResponse> chapters(long userId) {
        Map<Long, UserMastery> byKp = masteryByKp(userId);
        return seedContentRepository.listChapters().stream()
                .map(chapter -> toChapterProgress(chapter, byKp))
                .sorted(Comparator.comparingInt(ChapterProgressResponse::averageScore))
                .toList();
    }

    private ChapterProgressResponse toChapterProgress(Chapter chapter, Map<Long, UserMastery> byKp) {
        List<KnowledgePoint> knowledgePoints = seedContentRepository.listKnowledgePoints().stream()
                .filter(kp -> kp.chapterId() == chapter.id())
                .toList();
        int total = knowledgePoints.size();
        int studied = 0;
        int mastered = 0;
        int weak = 0;
        int sum = 0;
        for (KnowledgePoint kp : knowledgePoints) {
            UserMastery mastery = byKp.get(kp.id());
            int score = mastery == null ? 0 : mastery.masteryScore();
            boolean studiedKp = mastery != null && mastery.studyCount() > 0;
            if (studiedKp) {
                studied++;
            }
            if (score >= 70) {
                mastered++;
            }
            if (studiedKp && score < 60) {
                weak++;
            }
            sum += score;
        }
        int average = total == 0 ? 0 : Math.round((float) sum / total);
        boolean chapterWeak = studied > 0 && average < 60;
        return new ChapterProgressResponse(chapter.id(), chapter.title(), total, studied, mastered, weak, average, chapterWeak);
    }

    private Map<Long, UserMastery> masteryByKp(long userId) {
        return userMasteryRepository.listByUser(userId).stream()
                .collect(Collectors.toMap(UserMastery::knowledgePointId, Function.identity()));
    }
}
