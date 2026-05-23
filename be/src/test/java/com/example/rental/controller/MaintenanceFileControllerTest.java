package com.example.rental.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@DisplayName("MaintenanceFileController tests")
class MaintenanceFileControllerTest extends AbstractIntegrationTest {

    private final Path uploadsDir = Path.of("uploads", "maintenance").toAbsolutePath();

    @AfterEach
    void cleanup() throws IOException {
        if (Files.exists(uploadsDir)) {
            Files.list(uploadsDir).forEach(p -> p.toFile().delete());
        }
    }

    @Test
    void serveImage_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/maintenance/images/does-not-exist.jpg"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void serveImage_existingFile_shouldReturn200() throws Exception {
        Files.createDirectories(uploadsDir);
        Path f = uploadsDir.resolve("test.jpg");
        Files.write(f, "x".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/maintenance/images/test.jpg"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
