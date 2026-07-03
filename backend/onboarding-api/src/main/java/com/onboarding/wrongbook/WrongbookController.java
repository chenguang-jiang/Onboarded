package com.onboarding.wrongbook;

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
@RequestMapping("/api/wrongbook")
public class WrongbookController {

    private final SimpleTokenService tokenService;
    private final WrongbookService wrongbookService;

    public WrongbookController(SimpleTokenService tokenService, WrongbookService wrongbookService) {
        this.tokenService = tokenService;
        this.wrongbookService = wrongbookService;
    }

    @GetMapping
    public ApiResponse<WrongbookResponse> list(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(wrongbookService.listWrongbook(userId));
    }

    @GetMapping("/chapters")
    public ApiResponse<List<WrongbookChapterResponse>> chapters(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(wrongbookService.listChapters(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<WrongQuestionListItem> detail(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(wrongbookService.getDetail(userId, id));
    }

    @PostMapping("/{id}/redo")
    public ApiResponse<RedoResponse> redo(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody RedoRequest request
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(wrongbookService.redo(userId, id, request));
    }

    @PostMapping("/{id}/mastered")
    public ApiResponse<MasteredResponse> mastered(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(wrongbookService.markMastered(userId, id));
    }
}
