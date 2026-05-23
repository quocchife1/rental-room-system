package com.example.rental.controller;

import com.example.rental.dto.branch.BranchResponse;
import com.example.rental.service.BranchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("PublicBranchController – Integration Tests")
class PublicBranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BranchService branchService;

    @Nested
    @DisplayName("GET /api/public/branches")
    class GetAllBranchesTests {

        @Test
        @DisplayName("✅ Lấy danh sách chi nhánh công khai → 200 + list")
        void getAllBranches_shouldReturn200() throws Exception {
            BranchResponse branch = BranchResponse.builder()
                    .id(1L)
                    .branchCode("CN01")
                    .branchName("Chi nhanh Q1")
                    .address("123 Le Loi")
                    .phoneNumber("0281234567")
                    .build();

            when(branchService.getAllBranches()).thenReturn(List.of(branch));

            mockMvc.perform(get("/api/public/branches"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].branchCode").value("CN01"));
        }

        @Test
        @DisplayName("✅ Danh sách rỗng → 200 + []")
        void getAllBranches_empty_shouldReturn200() throws Exception {
            when(branchService.getAllBranches()).thenReturn(List.of());

            mockMvc.perform(get("/api/public/branches"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }
}
