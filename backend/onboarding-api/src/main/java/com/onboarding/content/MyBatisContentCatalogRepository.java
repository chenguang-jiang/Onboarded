package com.onboarding.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("local")
public class MyBatisContentCatalogRepository implements ContentCatalogRepository {

    private final ChapterMapper chapterMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final PracticeQuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;

    public MyBatisContentCatalogRepository(
            ChapterMapper chapterMapper,
            KnowledgePointMapper knowledgePointMapper,
            PracticeQuestionMapper questionMapper,
            QuestionOptionMapper optionMapper
    ) {
        this.chapterMapper = chapterMapper;
        this.knowledgePointMapper = knowledgePointMapper;
        this.questionMapper = questionMapper;
        this.optionMapper = optionMapper;
    }

    @Override
    public List<Chapter> listChapters() {
        return chapterMapper.selectList(new LambdaQueryWrapper<ChapterEntity>().orderByAsc(ChapterEntity::getSortOrder))
                .stream()
                .map(ChapterEntity::toRecord)
                .toList();
    }

    @Override
    public List<KnowledgePoint> listKnowledgePoints() {
        return knowledgePointMapper.selectList(new LambdaQueryWrapper<KnowledgePointEntity>().orderByAsc(KnowledgePointEntity::getId))
                .stream()
                .map(KnowledgePointEntity::toRecord)
                .toList();
    }

    @Override
    public Optional<Chapter> getChapter(long chapterId) {
        ChapterEntity entity = chapterMapper.selectById(chapterId);
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }

    @Override
    public Optional<KnowledgePoint> getKnowledgePointById(long knowledgePointId) {
        KnowledgePointEntity entity = knowledgePointMapper.selectById(knowledgePointId);
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }

    @Override
    public Optional<PracticeQuestion> getQuestionByKnowledgePointId(long knowledgePointId) {
        PracticeQuestionEntity entity = questionMapper.selectOne(
                new LambdaQueryWrapper<PracticeQuestionEntity>()
                        .eq(PracticeQuestionEntity::getKnowledgePointId, knowledgePointId)
                        .last("limit 1"));
        return toQuestion(entity);
    }

    @Override
    public Optional<PracticeQuestion> getQuestionById(long questionId) {
        return toQuestion(questionMapper.selectById(questionId));
    }

    private Optional<PracticeQuestion> toQuestion(PracticeQuestionEntity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        List<QuestionOption> options = optionMapper.selectList(
                        new LambdaQueryWrapper<QuestionOptionEntity>()
                                .eq(QuestionOptionEntity::getQuestionId, entity.getId())
                                .orderByAsc(QuestionOptionEntity::getSortOrder))
                .stream()
                .sorted(Comparator.comparing(QuestionOptionEntity::getSortOrder))
                .map(QuestionOptionEntity::toRecord)
                .toList();
        return Optional.of(new PracticeQuestion(
                entity.getId(),
                entity.getKnowledgePointId(),
                entity.getStem(),
                options,
                entity.getAnswerKey(),
                entity.getExplanation()
        ));
    }
}
