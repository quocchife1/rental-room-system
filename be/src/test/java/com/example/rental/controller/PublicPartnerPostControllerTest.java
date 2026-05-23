package com.example.rental.controller;

import com.example.rental.entity.PartnerPost;
import com.example.rental.entity.Partners;
import com.example.rental.entity.PostApprovalStatus;
import com.example.rental.entity.PostImage;
import com.example.rental.entity.PostType;
import com.example.rental.repository.PostImageRepository;
import com.example.rental.service.PartnerPostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("PublicPartnerPostController – Integration Tests")
class PublicPartnerPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PartnerPostService partnerPostService;

    @MockitoBean
    private PostImageRepository postImageRepository;

    private PartnerPost samplePost;

    @BeforeEach
    void setUp() {
        Partners partner = Partners.builder()
                .id(1L)
                .companyName("Nha tro A")
                .phoneNumber("0909123456")
                .username("partner_a")
                .address("HCM")
                .password("pwd")
                .contactPerson("A")
                .build();

        samplePost = PartnerPost.builder()
                .id(10L)
                .partner(partner)
                .title("Phong dep")
                .description("Mo ta")
                .price(new BigDecimal("3500000"))
                .area(new BigDecimal("25"))
                .address("Q1")
                .postType(PostType.NORMAL)
                .status(PostApprovalStatus.APPROVED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // =========================================================
    // 1. GET /api/public/partner-posts
    // =========================================================
    @Nested
    @DisplayName("GET /api/public/partner-posts")
    class ListApprovedTests {

        @Test
        @DisplayName("✅ Lấy danh sách tin hiển thị → 200 + page")
        void listApproved_shouldReturn200() throws Exception {
            Page<PartnerPost> page = new PageImpl<>(List.of(samplePost));
            when(partnerPostService.findPublicVisiblePosts(any(Pageable.class))).thenReturn(page);
            when(postImageRepository.findByPostId(10L))
                    .thenReturn(List.of(PostImage.builder().imageUrl("/uploads/partner-posts/a.jpg").build()));

            mockMvc.perform(get("/api/public/partner-posts")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].title").value("Phong dep"));
        }
    }

    // =========================================================
    // 2. GET /api/public/partner-posts/{id}
    // =========================================================
    @Nested
    @DisplayName("GET /api/public/partner-posts/{id}")
    class GetPublicPostTests {

        @Test
        @DisplayName("✅ Lấy chi tiết tin hiển thị → 200 + increment views")
        void getPublicPost_shouldReturn200() throws Exception {
            samplePost.setViews(1);
            when(partnerPostService.findById(10L)).thenReturn(Optional.of(samplePost));
            when(postImageRepository.findByPostId(10L))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/public/partner-posts/10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(10));

            verify(partnerPostService, times(1)).savePost(any(PartnerPost.class));
        }

        @Test
        @DisplayName("❌ Tin bi xoa → 404")
        void getPublicPost_deleted_shouldReturn404() throws Exception {
            samplePost.setDeleted(true);
            when(partnerPostService.findById(10L)).thenReturn(Optional.of(samplePost));

            mockMvc.perform(get("/api/public/partner-posts/10"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.statusCode").value(404));
        }

        @Test
        @DisplayName("❌ Tin khong ton tai → 404")
        void getPublicPost_notFound_shouldReturn404() throws Exception {
            when(partnerPostService.findById(9999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/public/partner-posts/9999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.statusCode").value(404));
        }
    }
}
