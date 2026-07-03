package com.onboarding.content;

import com.onboarding.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final SeedContentRepository seedContentRepository;

    public ContentController(SeedContentRepository seedContentRepository) {
        this.seedContentRepository = seedContentRepository;
    }

    @GetMapping("/chapters")
    public ApiResponse<List<ChapterResponse>> listChapters() {
        return ApiResponse.ok(seedContentRepository.listChapterResponses());
    }
}
