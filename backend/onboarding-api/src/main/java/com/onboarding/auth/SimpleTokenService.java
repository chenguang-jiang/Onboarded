package com.onboarding.auth;

import com.onboarding.user.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimpleTokenService {

    private static final String TOKEN_PREFIX = "ob-session-";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REDIS_KEY_PREFIX = "onboarding:session:";
    private static final Duration SESSION_TTL = Duration.ofDays(30);

    private final UserAccountRepository userAccountRepository;
    private final StringRedisTemplate redisTemplate;
    private final Map<String, Long> inMemorySessions = new ConcurrentHashMap<>();

    public SimpleTokenService(
            UserAccountRepository userAccountRepository,
            ObjectProvider<StringRedisTemplate> redisTemplateProvider
    ) {
        this.userAccountRepository = userAccountRepository;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
    }

    public String issueToken(long userId) {
        String token = TOKEN_PREFIX + UUID.randomUUID();
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(redisKey(token), Long.toString(userId), SESSION_TTL);
        } else {
            inMemorySessions.put(token, userId);
        }
        return token;
    }

    public long requireUserId(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "missing bearer token");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());
        if (!token.startsWith(TOKEN_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token");
        }

        Long userId = resolveSession(token);
        if (userId == null || userAccountRepository.findById(userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token");
        }
        return userId;
    }

    private Long resolveSession(String token) {
        if (redisTemplate != null) {
            String stored = redisTemplate.opsForValue().get(redisKey(token));
            if (!StringUtils.hasText(stored)) {
                return null;
            }
            redisTemplate.expire(redisKey(token), SESSION_TTL);
            try {
                return Long.parseLong(stored);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return inMemorySessions.get(token);
    }

    private static String redisKey(String token) {
        return REDIS_KEY_PREFIX + token;
    }
}
