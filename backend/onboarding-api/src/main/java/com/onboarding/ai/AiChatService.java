package com.onboarding.ai;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class AiChatService {

    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;
    private final AiReviewItemRepository reviewItemRepository;
    private final GlmClient glmClient;
    private final AiRateLimiter rateLimiter;

    public AiChatService(
            AiChatSessionRepository sessionRepository,
            AiChatMessageRepository messageRepository,
            AiReviewItemRepository reviewItemRepository,
            GlmClient glmClient,
            AiRateLimiter rateLimiter
    ) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.reviewItemRepository = reviewItemRepository;
        this.glmClient = glmClient;
        this.rateLimiter = rateLimiter;
    }

    public AiChatSessionResponse createSession(long userId, CreateSessionRequest request) {
        Instant now = Instant.now();
        String title = request == null || request.title() == null || request.title().isBlank()
                ? defaultTitle(now)
                : request.title();
        AiChatSession session = sessionRepository.save(AiChatSession.newSession(userId, title, now));
        return toSessionResponse(session);
    }

    public List<AiChatSessionResponse> listSessions(long userId) {
        return sessionRepository.listByUser(userId).stream()
                .sorted(Comparator.comparing(AiChatSession::updatedAt).reversed())
                .map(this::toSessionResponse)
                .toList();
    }

    public List<AiChatMessageResponse> listMessages(long userId, long sessionId) {
        requireOwnedSession(userId, sessionId);
        return messageRepository.findBySession(sessionId).stream()
                .sorted(Comparator.comparing(AiChatMessage::createdAt))
                .map(this::toMessageResponse)
                .toList();
    }

    public AiChatMessageResponse ask(long userId, long sessionId, AskRequest request) {
        AiChatSession session = requireOwnedSession(userId, sessionId);
        if (!rateLimiter.tryAcquire(userId)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "今日提问次数已达上限，请稍后再试");
        }
        Instant now = Instant.now();
        messageRepository.save(AiChatMessage.newUser(sessionId, userId, request.question(), now));
        GlmAnswer answer = glmClient.ask(request.question());
        AiChatMessage assistant = messageRepository.save(AiChatMessage.newAssistant(
                sessionId, userId, answer.content(), answer.references(), answer.tokensUsed(), Instant.now()
        ));
        sessionRepository.save(session.touch(Instant.now()));
        return toMessageResponse(assistant);
    }

    public AiReviewItemResponse addMessageToReviewQueue(long userId, long messageId) {
        AiChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "message not found"));
        if (message.userId() != userId || !AiChatMessage.ROLE_ASSISTANT.equals(message.role())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "message not found");
        }
        AiReviewItem item = reviewItemRepository.findByUserAndMessage(userId, messageId)
                .orElseGet(() -> reviewItemRepository.save(AiReviewItem.pending(userId, message, Instant.now())));
        return toReviewItemResponse(item);
    }

    private AiChatSession requireOwnedSession(long userId, long sessionId) {
        AiChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "session not found"));
        if (session.userId() != userId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "session not found");
        }
        return session;
    }

    private AiChatSessionResponse toSessionResponse(AiChatSession session) {
        return new AiChatSessionResponse(session.id(), session.title(), session.createdAt().toString());
    }

    private AiChatMessageResponse toMessageResponse(AiChatMessage message) {
        return new AiChatMessageResponse(
                message.id(),
                message.role(),
                message.content(),
                message.references(),
                message.tokensUsed(),
                message.createdAt().toString()
        );
    }

    private AiReviewItemResponse toReviewItemResponse(AiReviewItem item) {
        return new AiReviewItemResponse(
                item.id(),
                item.messageId(),
                item.status(),
                item.createdAt().toString()
        );
    }

    private static String defaultTitle(Instant now) {
        return "答疑 " + now.toString().substring(0, 10);
    }
}
