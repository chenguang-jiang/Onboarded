package com.onboarding.ai;

import com.onboarding.auth.SimpleTokenService;
import com.onboarding.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final SimpleTokenService tokenService;
    private final AiChatService aiChatService;

    public AiChatController(SimpleTokenService tokenService, AiChatService aiChatService) {
        this.tokenService = tokenService;
        this.aiChatService = aiChatService;
    }

    @PostMapping("/sessions")
    public ApiResponse<AiChatSessionResponse> createSession(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) CreateSessionRequest request
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(aiChatService.createSession(userId, request));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<AiChatSessionResponse>> listSessions(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(aiChatService.listSessions(userId));
    }

    @GetMapping("/sessions/{id}/messages")
    public ApiResponse<List<AiChatMessageResponse>> listMessages(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(aiChatService.listMessages(userId, id));
    }

    @PostMapping("/sessions/{id}/ask")
    public ApiResponse<AiChatMessageResponse> ask(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody AskRequest request
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(aiChatService.ask(userId, id, request));
    }

    @PostMapping("/messages/{id}/review-items")
    public ApiResponse<AiReviewItemResponse> addToReviewQueue(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(aiChatService.addMessageToReviewQueue(userId, id));
    }
}
