package com.example.rental.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("PartnerPostImageController – Integration Tests")
class PartnerPostImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("✅ Serve existing image → 200")
    void serveImage_existing_shouldReturn200() throws Exception {
        Path dir = Paths.get("uploads", "partner-posts");
        Files.createDirectories(dir);
        Path file = dir.resolve("test-image.jpg");
        Files.write(file, "dummy".getBytes());

        try {
            mockMvc.perform(get("/api/partner-posts/images/test-image.jpg"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("test-image.jpg")));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    @DisplayName("❌ Image not found → 404")
    void serveImage_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/partner-posts/images/not-exist.jpg"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
