package com.onboarding.progress;

import com.onboarding.auth.SimpleTokenService;
import com.onboarding.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final SimpleTokenService tokenService;
    private final MasteryService masteryService;

    public ProgressController(SimpleTokenService tokenService, MasteryService masteryService) {
        this.tokenService = tokenService;
        this.masteryService = masteryService;
    }

    @GetMapping("/overview")
    public ApiResponse<ProgressOverviewResponse> overview(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(masteryService.overview(userId));
    }

    @GetMapping("/chapters")
    public ApiResponse<List<ChapterProgressResponse>> chapters(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(masteryService.chapters(userId));
    }
}
