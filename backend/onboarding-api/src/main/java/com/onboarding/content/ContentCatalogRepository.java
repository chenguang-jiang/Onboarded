package com.onboarding.content;

import java.util.List;
import java.util.Optional;

public interface ContentCatalogRepository {

    List<Chapter> listChapters();

    List<KnowledgePoint> listKnowledgePoints();

    Optional<Chapter> getChapter(long chapterId);

    Optional<KnowledgePoint> getKnowledgePointById(long knowledgePointId);

    Optional<PracticeQuestion> getQuestionByKnowledgePointId(long knowledgePointId);

    Optional<PracticeQuestion> getQuestionById(long questionId);
}
