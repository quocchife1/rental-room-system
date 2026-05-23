package com.example.rental.controller;

import com.example.rental.utils.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@DisplayName("FileUploadController tests")
class FileUploadControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @WithMockUser(roles = "MANAGER") // Đóng giả user có quyền
    void uploadContract_shouldReturn200() throws Exception {
        when(fileStorageService.storeFile(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("contracts")))
                .thenReturn("/uploads/contracts/abc.pdf");

        MockMultipartFile file = new MockMultipartFile("file", "abc.pdf", "application/pdf", "x".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/upload/contract")
                        .file(file)
                        .with(csrf())) // <--- QUAN TRỌNG: Thêm CSRF Token để không bị 403
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists());
    }
}
