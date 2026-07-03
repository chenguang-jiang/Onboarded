package com.onboarding.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("local")
public class MyBatisAiChatMessageRepository implements AiChatMessageRepository {

    private final AiChatMessageMapper mapper;

    public MyBatisAiChatMessageRepository(AiChatMessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AiChatMessage save(AiChatMessage message) {
        AiChatMessageEntity entity = AiChatMessageEntity.from(message);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return entity.toRecord();
    }

    @Override
    public Optional<AiChatMessage> findById(long id) {
        AiChatMessageEntity entity = mapper.selectById(id);
        return entity == null ? Optional.empty() : Optional.of(entity.toRecord());
    }

    @Override
    public List<AiChatMessage> findBySession(long sessionId) {
        return mapper.selectList(
                new LambdaQueryWrapper<AiChatMessageEntity>().eq(AiChatMessageEntity::getSessionId, sessionId))
                .stream()
                .map(AiChatMessageEntity::toRecord)
                .toList();
    }
}
