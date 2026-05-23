package com.example.rental.controller;

import com.example.rental.dto.partnerpost.PartnerPostCreateRequest;
import com.example.rental.entity.PartnerPost;
import com.example.rental.entity.Partners;
import com.example.rental.entity.PostApprovalStatus;
import com.example.rental.entity.PostImage;
import com.example.rental.entity.PostType;
import com.example.rental.repository.PartnerRepository;
import com.example.rental.repository.PostImageRepository;
import com.example.rental.service.MomoService;
import com.example.rental.service.PartnerPostService;
import com.example.rental.utils.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("PartnerPostController – Integration Tests")
class PartnerPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PartnerPostService partnerPostService;

    @MockitoBean
    private PartnerRepository partnerRepository;

    @MockitoBean
    private PostImageRepository postImageRepository;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private MomoService momoService;

    private Partners partner;
    private PartnerPost samplePost;

    @BeforeEach
    void setUp() {
        partner = Partners.builder()
                .id(1L)
                .username("partner1")
                .companyName("Nha tro A")
                .phoneNumber("0909123456")
                .password("pwd")
                .contactPerson("A")
                .address("HCM")
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
                .status(PostApprovalStatus.PENDING_APPROVAL)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // =========================================================
    // 1. GET /api/partner-posts/my-posts
    // =========================================================
    @Nested
    @DisplayName("GET /api/partner-posts/my-posts")
    class GetMyPostsTests {

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("✅ PARTNER xem danh sach tin → 200 + list")
        void getMyPosts_shouldReturn200() throws Exception {
            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            when(partnerPostService.findPostsByPartnerId(1L)).thenReturn(List.of(samplePost));
            when(postImageRepository.findByPostId(10L)).thenReturn(List.of());

            mockMvc.perform(get("/api/partner-posts/my-posts"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)));
        }

        @Test
        @WithMockUser(roles = "TENANT")
        @DisplayName("❌ TENANT xem my-posts → 403")
        void getMyPosts_asTenant_shouldReturn403() throws Exception {
            mockMvc.perform(get("/api/partner-posts/my-posts"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================
    // 2. GET /api/partner-posts/my-posts/paged
    // =========================================================
    @Nested
    @DisplayName("GET /api/partner-posts/my-posts/paged")
    class GetMyPostsPagedTests {

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("✅ PARTNER xem my-posts/paged → 200 + page")
        void getMyPostsPaged_shouldReturn200() throws Exception {
            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            org.springframework.data.domain.Page<PartnerPost> page =
                    new org.springframework.data.domain.PageImpl<>(List.of(samplePost));
            when(partnerPostService.findPostsByPartnerId(eq(1L), any())).thenReturn(page);
            when(postImageRepository.findByPostId(10L)).thenReturn(List.of());

            mockMvc.perform(get("/api/partner-posts/my-posts/paged")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content", hasSize(1)));
        }
    }

    // =========================================================
    // 3. GET /api/partner-posts/my-posts/stats/monthly-views
    // =========================================================
    @Nested
    @DisplayName("GET /api/partner-posts/my-posts/stats/monthly-views")
    class MonthlyViewsTests {

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("✅ PARTNER xem thong ke views → 200")
        void getMonthlyViews_shouldReturn200() throws Exception {
            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            when(partnerPostService.findPostsByPartnerId(1L)).thenReturn(List.of(samplePost));

            mockMvc.perform(get("/api/partner-posts/my-posts/stats/monthly-views"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // =========================================================
    // 4. POST /api/partner-posts (multipart)
    // =========================================================
    @Nested
    @DisplayName("POST /api/partner-posts")
    class CreatePostTests {

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("✅ Create post → 201")
        void createPost_shouldReturn201() throws Exception {
            PartnerPostCreateRequest req = new PartnerPostCreateRequest(
                    "Tieu de",
                    "Mo ta",
                    new BigDecimal("3500000"),
                    new BigDecimal("25"),
                    "Q1",
                    PostType.NORMAL
            );

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "data.json",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(req)
            );
            MockMultipartFile image = new MockMultipartFile(
                    "images",
                    "photo.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "dummy".getBytes()
            );

            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            when(momoService.createATMPayment(anyLong(), anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(com.example.rental.dto.momo.CreateMomoResponse.builder().payUrl("https://momo.test/pay").build());
            PartnerPost saved = PartnerPost.builder().id(10L).partner(partner).postType(PostType.NORMAL).build();
            when(partnerPostService.createPost(any())).thenReturn(saved);
            when(fileStorageService.storeFile(any(), eq("partner-posts"))).thenReturn("img.jpg");
            when(postImageRepository.findByPostId(10L)).thenReturn(List.of(PostImage.builder().imageUrl("/uploads/partner-posts/img.jpg").build()));

            mockMvc.perform(multipart("/api/partner-posts")
                            .file(data)
                            .file(image))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.message").value("Tạo tin đăng thành công. Tin đăng đang chờ duyệt."));
        }

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("❌ Qua 5 anh → 400")
        void createPost_tooManyImages_shouldReturn400() throws Exception {
            PartnerPostCreateRequest req = new PartnerPostCreateRequest(
                    "Tieu de",
                    "Mo ta",
                    new BigDecimal("3500000"),
                    new BigDecimal("25"),
                    "Q1",
                    PostType.NORMAL
            );
            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "data.json",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(req)
            );

            MockMultipartFile[] images = new MockMultipartFile[6];
            for (int i = 0; i < 6; i++) {
                images[i] = new MockMultipartFile(
                        "images",
                        "img" + i + ".jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        "x".getBytes()
                );
            }

            var builder = multipart("/api/partner-posts").file(data);
            for (MockMultipartFile img : images) {
                builder.file(img);
            }

            mockMvc.perform(builder)
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));
        }
    }

    // =========================================================
    // 5. POST /api/partner-posts/{id}/momo/initiate
    // =========================================================
    @Nested
    @DisplayName("POST /api/partner-posts/{id}/momo/initiate")
    class InitiateMomoTests {

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("❌ Khong phai chu tin → 403")
        void initiateMomo_notOwner_shouldReturn403() throws Exception {
            Partners other = Partners.builder().id(2L).username("other").password("pwd").contactPerson("B").address("HCM").companyName("B").build();
            PartnerPost post = PartnerPost.builder().id(10L).partner(other).status(PostApprovalStatus.PENDING_PAYMENT).postType(PostType.NORMAL).build();

            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            when(partnerPostService.findById(10L)).thenReturn(Optional.of(post));

            mockMvc.perform(post("/api/partner-posts/10/momo/initiate"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.statusCode").value(403));
        }

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("❌ Tin khong o trang thai PENDING_PAYMENT → 400")
        void initiateMomo_invalidStatus_shouldReturn400() throws Exception {
            PartnerPost post = PartnerPost.builder().id(10L).partner(partner).status(PostApprovalStatus.APPROVED).postType(PostType.NORMAL).build();

            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            when(partnerPostService.findById(10L)).thenReturn(Optional.of(post));

            mockMvc.perform(post("/api/partner-posts/10/momo/initiate"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));
        }

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("✅ Initiate momo → 200 + payUrl")
        void initiateMomo_shouldReturn200() throws Exception {
            PartnerPost post = PartnerPost.builder().id(10L).partner(partner).status(PostApprovalStatus.PENDING_PAYMENT).postType(PostType.NORMAL).build();

            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            when(partnerPostService.findById(10L)).thenReturn(Optional.of(post));
            when(momoService.createATMPayment(anyLong(), anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(com.example.rental.dto.momo.CreateMomoResponse.builder().payUrl("https://momo.test/pay").build());

            mockMvc.perform(post("/api/partner-posts/10/momo/initiate"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.payUrl").value("https://momo.test/pay"));
        }
    }

    // =========================================================
    // 6. GET /api/partner-posts/{id}
    // =========================================================
    @Nested
    @DisplayName("GET /api/partner-posts/{id}")
    class GetPostByIdTests {

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("✅ PARTNER xem tin cua minh → 200")
        void getPostById_owner_shouldReturn200() throws Exception {
            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            when(partnerPostService.findById(10L)).thenReturn(Optional.of(samplePost));
            when(postImageRepository.findByPostId(10L)).thenReturn(List.of());

            mockMvc.perform(get("/api/partner-posts/10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(10));
        }

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("❌ PARTNER xem tin nguoi khac → 403")
        void getPostById_notOwner_shouldReturn403() throws Exception {
            Partners other = Partners.builder().id(2L).username("other").password("pwd").contactPerson("B").address("HCM").companyName("B").build();
            PartnerPost post = PartnerPost.builder().id(10L).partner(other).build();

            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            when(partnerPostService.findById(10L)).thenReturn(Optional.of(post));

            mockMvc.perform(get("/api/partner-posts/10"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.statusCode").value(403));
        }
    }

    // =========================================================
    // 7. PUT /api/partner-posts/{id}
    // =========================================================
    @Nested
    @DisplayName("PUT /api/partner-posts/{id}")
    class UpdatePostTests {

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("❌ Tin da duyet → 400")
        void updatePost_approved_shouldReturn400() throws Exception {
            PartnerPostCreateRequest req = new PartnerPostCreateRequest(
                    "Tieu de",
                    "Mo ta",
                    new BigDecimal("3500000"),
                    new BigDecimal("25"),
                    "Q1",
                    PostType.NORMAL
            );

            PartnerPost approved = PartnerPost.builder().id(10L).partner(partner).status(PostApprovalStatus.APPROVED).postType(PostType.NORMAL).build();

            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            when(partnerPostService.findById(10L)).thenReturn(Optional.of(approved));

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "data.json",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(req)
            );

            mockMvc.perform(multipart("/api/partner-posts/10")
                            .file(data)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));
        }
    }

    // =========================================================
    // 8. DELETE /api/partner-posts/{id}
    // =========================================================
    @Nested
    @DisplayName("DELETE /api/partner-posts/{id}")
    class DeletePostTests {

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("❌ Xoa tin da duyet → 400")
        void deletePost_approved_shouldReturn400() throws Exception {
            PartnerPost approved = PartnerPost.builder().id(10L).partner(partner).status(PostApprovalStatus.APPROVED).build();

            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            when(partnerPostService.findById(10L)).thenReturn(Optional.of(approved));

            mockMvc.perform(delete("/api/partner-posts/10"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400));
        }

        @Test
        @WithMockUser(username = "partner1", roles = "PARTNER")
        @DisplayName("❌ Xoa tin nguoi khac → 403")
        void deletePost_notOwner_shouldReturn403() throws Exception {
            Partners other = Partners.builder().id(2L).username("other").password("pwd").contactPerson("B").address("HCM").companyName("B").build();
            PartnerPost post = PartnerPost.builder().id(10L).partner(other).status(PostApprovalStatus.PENDING_APPROVAL).build();

            when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));
            when(partnerPostService.findById(10L)).thenReturn(Optional.of(post));

            mockMvc.perform(delete("/api/partner-posts/10"))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.statusCode").value(403));
        }
    }
}
