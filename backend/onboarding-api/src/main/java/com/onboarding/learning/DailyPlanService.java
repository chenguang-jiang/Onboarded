package com.onboarding.learning;

import com.onboarding.content.Chapter;
import com.onboarding.content.KnowledgePoint;
import com.onboarding.content.PracticeQuestion;
import com.onboarding.content.SeedContentRepository;
import com.onboarding.progress.UserMastery;
import com.onboarding.progress.UserMasteryRepository;
import com.onboarding.wrongbook.WrongQuestion;
import com.onboarding.wrongbook.WrongQuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DailyPlanService {

    private static final int DAILY_KNOWLEDGE_COUNT = 15;
    private static final int LOW_MASTERY_THRESHOLD = 60;

    private final DailyPlanRepository dailyPlanRepository;
    private final DailyPlanItemRepository dailyPlanItemRepository;
    private final SeedContentRepository seedContentRepository;
    private final UserMasteryRepository userMasteryRepository;
    private final WrongQuestionRepository wrongQuestionRepository;

    public DailyPlanService(
            DailyPlanRepository dailyPlanRepository,
            DailyPlanItemRepository dailyPlanItemRepository,
            SeedContentRepository seedContentRepository,
            UserMasteryRepository userMasteryRepository,
            WrongQuestionRepository wrongQuestionRepository
    ) {
        this.dailyPlanRepository = dailyPlanRepository;
        this.dailyPlanItemRepository = dailyPlanItemRepository;
        this.seedContentRepository = seedContentRepository;
        this.userMasteryRepository = userMasteryRepository;
        this.wrongQuestionRepository = wrongQuestionRepository;
    }

    public TodayPlanResponse getTodayPlan(long userId) {
        DailyPlan plan = getOrCreateTodayPlan(userId);
        return toResponse(plan);
    }

    public TodayPlanResponse startItem(long userId, long itemId) {
        DailyPlanItem item = requireOwnedItem(userId, itemId);
        dailyPlanItemRepository.save(item.markStudying(Instant.now()));
        return toResponse(requirePlan(item.planId()));
    }

    public TodayPlanResponse completeItem(long userId, long itemId) {
        DailyPlanItem item = requireOwnedItem(userId, itemId);
        boolean wasDone = DailyPlanItem.STATUS_DONE.equals(item.status());
        dailyPlanItemRepository.save(item.markComplete(Instant.now()));
        DailyPlan plan = requirePlan(item.planId());
        if (!wasDone) {
            plan = dailyPlanRepository.save(plan.withCompletedCount(plan.completedCount() + 1, Instant.now()));
        }
        return toResponse(plan);
    }

    private DailyPlan getOrCreateTodayPlan(long userId) {
        LocalDate today = LocalDate.now();
        return dailyPlanRepository.findByUserAndDate(userId, today)
                .orElseGet(() -> generatePlan(userId, today));
    }

    private DailyPlan generatePlan(long userId, LocalDate today) {
        Instant now = Instant.now();
        List<KnowledgePoint> all = seedContentRepository.listKnowledgePoints();
        Map<Long, UserMastery> masteryByKp = userMasteryRepository.listByUser(userId).stream()
                .collect(Collectors.toMap(UserMastery::knowledgePointId, Function.identity()));
        Set<Long> wrongKps = wrongQuestionRepository.listByUser(userId).stream()
                .filter(wq -> !wq.mastered())
                .map(WrongQuestion::knowledgePointId)
                .collect(Collectors.toSet());

        List<PlanCandidate> carryOvers = carryOverCandidates(userId, today);
        Set<Long> carriedKps = carryOvers.stream()
                .map(PlanCandidate::knowledgePointId)
                .collect(Collectors.toSet());

        List<PlanCandidate> generated = all.stream()
                .filter(kp -> !carriedKps.contains(kp.id()))
                .sorted(Comparator
                        .comparingInt((KnowledgePoint kp) -> sourcePriority(sourceFor(kp, masteryByKp, wrongKps)))
                        .thenComparingLong(KnowledgePoint::id))
                .map(kp -> {
                    PracticeQuestion question = seedContentRepository.getQuestionByKnowledgePointId(kp.id());
                    Long questionId = question == null ? null : question.id();
                    return new PlanCandidate(kp.id(), questionId, sourceFor(kp, masteryByKp, wrongKps));
                })
                .limit(Math.max(0, DAILY_KNOWLEDGE_COUNT - carryOvers.size()))
                .toList();
        List<PlanCandidate> ordered = new ArrayList<>(DAILY_KNOWLEDGE_COUNT);
        ordered.addAll(carryOvers.stream().limit(DAILY_KNOWLEDGE_COUNT).toList());
        ordered.addAll(generated);

        DailyPlan plan = dailyPlanRepository.save(DailyPlan.newPlan(userId, today, ordered.size(), now));
        for (int i = 0; i < ordered.size(); i++) {
            PlanCandidate candidate = ordered.get(i);
            dailyPlanItemRepository.save(DailyPlanItem.newItem(
                    plan.id(),
                    userId,
                    candidate.knowledgePointId(),
                    candidate.questionId(),
                    candidate.source(),
                    i,
                    now
            ));
        }
        return plan;
    }

    private List<PlanCandidate> carryOverCandidates(long userId, LocalDate today) {
        List<DailyPlan> historicalPlans = dailyPlanRepository.findBeforeDate(userId, today);
        List<Long> planIds = historicalPlans.stream().map(DailyPlan::id).toList();
        Map<Long, DailyPlan> plansById = historicalPlans.stream()
                .collect(Collectors.toMap(DailyPlan::id, Function.identity()));
        Map<Long, PlanCandidate> byKnowledgePoint = new LinkedHashMap<>();
        dailyPlanItemRepository.findByPlans(planIds).stream()
                .filter(item -> !DailyPlanItem.STATUS_DONE.equals(item.status()))
                .sorted(Comparator
                        .comparing((DailyPlanItem item) -> plansById.get(item.planId()).planDate()).reversed()
                        .thenComparingInt(DailyPlanItem::sortNo))
                .forEach(item -> byKnowledgePoint.putIfAbsent(
                        item.knowledgePointId(),
                        new PlanCandidate(item.knowledgePointId(), item.questionId(), DailyPlanItem.SOURCE_CARRY_OVER)
                ));
        return byKnowledgePoint.values().stream()
                .limit(DAILY_KNOWLEDGE_COUNT)
                .toList();
    }

    private TodayPlanResponse toResponse(DailyPlan plan) {
        List<DailyPlanItem> items = dailyPlanItemRepository.findByPlan(plan.id()).stream()
                .sorted(Comparator.comparingInt(DailyPlanItem::sortNo))
                .toList();
        List<DailyPlanItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();
        return new TodayPlanResponse(
                plan.id(),
                plan.planDate().toString(),
                plan.totalCount(),
                plan.completedCount(),
                itemResponses
        );
    }

    private DailyPlanItemResponse toItemResponse(DailyPlanItem item) {
        KnowledgePoint kp = seedContentRepository.getKnowledgePointById(item.knowledgePointId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "knowledge point not found"));
        Chapter chapter = seedContentRepository.getChapter(kp.chapterId());
        DailyPlanItemResponse.QuestionBrief questionBrief = null;
        if (item.questionId() != null) {
            PracticeQuestion question = seedContentRepository.getQuestionById(item.questionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "question not found"));
            questionBrief = new DailyPlanItemResponse.QuestionBrief(question.id(), question.stem(), question.options());
        }
        return new DailyPlanItemResponse(
                item.id(),
                kp.id(),
                chapter.id(),
                chapter.title(),
                new DailyPlanItemResponse.KnowledgeBrief(kp.id(), kp.title(), kp.summary(), kp.difficulty(), kp.tags()),
                questionBrief,
                item.status(),
                item.source()
        );
    }

    private DailyPlanItem requireOwnedItem(long userId, long itemId) {
        DailyPlanItem item = dailyPlanItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "plan item not found"));
        if (item.userId() != userId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "plan item not found");
        }
        return item;
    }

    private DailyPlan requirePlan(long planId) {
        return dailyPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "plan not found"));
    }

    private static String sourceFor(KnowledgePoint kp, Map<Long, UserMastery> masteryByKp, Set<Long> wrongKps) {
        if (wrongKps.contains(kp.id())) {
            return DailyPlanItem.SOURCE_WRONG_RELATED;
        }
        UserMastery mastery = masteryByKp.get(kp.id());
        if (mastery != null && mastery.studyCount() > 0) {
            return mastery.masteryScore() < LOW_MASTERY_THRESHOLD
                    ? DailyPlanItem.SOURCE_LOW_MASTERY
                    : DailyPlanItem.SOURCE_FALLBACK;
        }
        return DailyPlanItem.SOURCE_NEW;
    }

    private static int sourcePriority(String source) {
        return switch (source) {
            case DailyPlanItem.SOURCE_WRONG_RELATED -> 0;
            case DailyPlanItem.SOURCE_LOW_MASTERY -> 1;
            case DailyPlanItem.SOURCE_NEW -> 2;
            case DailyPlanItem.SOURCE_FALLBACK -> 3;
            default -> 4;
        };
    }

    private record PlanCandidate(long knowledgePointId, Long questionId, String source) {
    }
}
