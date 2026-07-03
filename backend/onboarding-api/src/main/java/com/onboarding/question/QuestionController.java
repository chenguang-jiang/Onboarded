package com.onboarding.question;

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

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final SimpleTokenService tokenService;
    private final AnswerService answerService;

    public QuestionController(SimpleTokenService tokenService, AnswerService answerService) {
        this.tokenService = tokenService;
        this.answerService = answerService;
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionDetailResponse> getQuestion(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        tokenService.requireUserId(authorization);
        return ApiResponse.ok(answerService.getQuestion(id));
    }

    @PostMapping("/{id}/answer")
    public ApiResponse<AnswerResponse> submitAnswer(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody AnswerRequest request
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(answerService.submitAnswer(userId, id, request));
    }
}
