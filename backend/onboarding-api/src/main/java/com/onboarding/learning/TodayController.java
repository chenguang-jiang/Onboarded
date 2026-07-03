package com.onboarding.learning;

import com.onboarding.auth.SimpleTokenService;
import com.onboarding.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/today")
public class TodayController {

    private final SimpleTokenService tokenService;
    private final DailyPlanService dailyPlanService;

    public TodayController(SimpleTokenService tokenService, DailyPlanService dailyPlanService) {
        this.tokenService = tokenService;
        this.dailyPlanService = dailyPlanService;
    }

    @GetMapping
    public ApiResponse<TodayPlanResponse> getToday(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(dailyPlanService.getTodayPlan(userId));
    }

    @PostMapping("/items/{itemId}/start")
    public ApiResponse<TodayPlanResponse> startItem(
            @PathVariable long itemId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(dailyPlanService.startItem(userId, itemId));
    }

    @PostMapping("/items/{itemId}/complete")
    public ApiResponse<TodayPlanResponse> completeItem(
            @PathVariable long itemId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(dailyPlanService.completeItem(userId, itemId));
    }
}
