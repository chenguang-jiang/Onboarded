package com.onboarding.notification;

import com.onboarding.auth.SimpleTokenService;
import com.onboarding.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final SimpleTokenService tokenService;
    private final NotificationService notificationService;

    public NotificationController(SimpleTokenService tokenService, NotificationService notificationService) {
        this.tokenService = tokenService;
        this.notificationService = notificationService;
    }

    @PutMapping("/subscription")
    public ApiResponse<SubscriptionResponse> saveSubscription(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody SubscriptionRequest request
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(notificationService.saveSubscription(userId, request));
    }
}
