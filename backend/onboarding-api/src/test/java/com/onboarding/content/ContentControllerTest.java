package com.onboarding.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentController.class)
@Import({
        SeedContentRepository.class,
        InMemoryContentCatalogRepository.class
})
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void chaptersReturnsCatalogBackedByContentRepository() throws Exception {
        mockMvc.perform(get("/api/content/chapters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.data[0].title").value("计算机系统基础"))
                .andExpect(jsonPath("$.data[0].knowledgeCount").value(3))
                .andExpect(jsonPath("$.data[0].questionCount").value(3));
    }
}
