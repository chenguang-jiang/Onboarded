package com.onboarding.content;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class SeedContentRepository {

    private final ContentCatalogRepository catalogRepository;

    public SeedContentRepository(ObjectProvider<ContentCatalogRepository> catalogRepositoryProvider) {
        this.catalogRepository = catalogRepositoryProvider.getIfAvailable(InMemoryContentCatalogRepository::new);
    }

    public List<ChapterResponse> listChapterResponses() {
        return listChapters().stream()
                .sorted(Comparator.comparingInt(Chapter::sortOrder))
                .map(chapter -> new ChapterResponse(
                        chapter.id(),
                        chapter.title(),
                        chapter.description(),
                        countKnowledge(chapter.id()),
                        countQuestions(chapter.id())
                ))
                .toList();
    }

    public List<KnowledgePoint> listKnowledgePoints() {
        return catalogRepository.listKnowledgePoints();
    }

    public List<Chapter> listChapters() {
        return catalogRepository.listChapters();
    }

    public Chapter getChapter(long chapterId) {
        return catalogRepository.getChapter(chapterId).orElseThrow();
    }

    public PracticeQuestion getQuestionByKnowledgePointId(long knowledgePointId) {
        return catalogRepository.getQuestionByKnowledgePointId(knowledgePointId).orElse(null);
    }

    public Optional<PracticeQuestion> getQuestionById(long questionId) {
        return catalogRepository.getQuestionById(questionId);
    }

    public Optional<KnowledgePoint> getKnowledgePointById(long knowledgePointId) {
        return catalogRepository.getKnowledgePointById(knowledgePointId);
    }

    private int countKnowledge(long chapterId) {
        return (int) listKnowledgePoints().stream()
                .filter(knowledgePoint -> knowledgePoint.chapterId() == chapterId)
                .count();
    }

    private int countQuestions(long chapterId) {
        return (int) listKnowledgePoints().stream()
                .filter(knowledgePoint -> knowledgePoint.chapterId() == chapterId)
                .map(KnowledgePoint::id)
                .filter(knowledgePointId -> getQuestionByKnowledgePointId(knowledgePointId) != null)
                .count();
    }
}
