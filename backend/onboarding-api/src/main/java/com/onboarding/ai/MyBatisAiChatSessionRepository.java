package com.onboarding.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("local")
public class MyBatisAiChatSessionRepository implements AiChatSessionRepository {

    private final AiChatSessionMapper mapper;

    public MyBatisAiChatSessionRepository(AiChatSessionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AiChatSession save(AiChatSession session) {
        AiChatSessionEntity entity = AiChatSessionEntity.from(session);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return entity.toRecord();
    }

    @Override
    public Optional<AiChatSession> findById(long id) {
        AiChatSessionEntity entity = mapper.selectById(id);
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }

    @Override
    public List<AiChatSession> listByUser(long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<AiChatSessionEntity>().eq(AiChatSessionEntity::getUserId, userId))
                .stream()
                .map(AiChatSessionEntity::toRecord)
                .toList();
    }
}
