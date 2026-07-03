package com.onboarding.user;

import com.onboarding.auth.SimpleTokenService;
import com.onboarding.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class StudySettingsController {

    private final SimpleTokenService tokenService;
    private final StudySettingsService studySettingsService;

    public StudySettingsController(SimpleTokenService tokenService, StudySettingsService studySettingsService) {
        this.tokenService = tokenService;
        this.studySettingsService = studySettingsService;
    }

    @PutMapping("/study-settings")
    public ApiResponse<StudySettingsResponse> updateStudySettings(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody StudySettingsRequest request
    ) {
        long userId = tokenService.requireUserId(authorization);
        return ApiResponse.ok(studySettingsService.updateStudySettings(userId, request));
    }
}
