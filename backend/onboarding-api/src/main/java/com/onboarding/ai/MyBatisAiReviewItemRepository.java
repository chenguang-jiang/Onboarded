package com.onboarding.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("local")
public class MyBatisAiReviewItemRepository implements AiReviewItemRepository {

    private final AiReviewItemMapper mapper;

    public MyBatisAiReviewItemRepository(AiReviewItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AiReviewItem save(AiReviewItem item) {
        AiReviewItemEntity entity = AiReviewItemEntity.from(item);
        if (entity.getId() == null) {
            mapper.insert(entity);
        }
        return entity.toRecord();
    }

    @Override
    public Optional<AiReviewItem> findByUserAndMessage(long userId, long messageId) {
        AiReviewItemEntity entity = mapper.selectOne(
                new LambdaQueryWrapper<AiReviewItemEntity>()
                        .eq(AiReviewItemEntity::getUserId, userId)
                        .eq(AiReviewItemEntity::getMessageId, messageId));
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }
}
