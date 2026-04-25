package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.partnerpost.PartnerPostCreateRequest;
import com.example.rental.dto.partnerpost.PartnerPostResponse;
import com.example.rental.entity.PartnerPost;
import com.example.rental.entity.Partners;
import com.example.rental.entity.PostApprovalStatus;
import com.example.rental.entity.PostImage;
import com.example.rental.entity.PostType;
import com.example.rental.exception.ResourceNotFoundException;
import com.example.rental.repository.PartnerRepository;
import com.example.rental.repository.PostImageRepository;
import com.example.rental.service.PartnerPostService;
import com.example.rental.service.MomoService;
import com.example.rental.utils.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/partner-posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Partner Posts", description = "API quản lý tin đăng của đối tác")
public class PartnerPostController {

        private final PartnerPostService partnerPostService;
        private final PartnerRepository partnerRepository;
        private final PostImageRepository postImageRepository;
        private final FileStorageService fileStorageService;
        private final ObjectMapper objectMapper;
        private final MomoService momoService;

        @Value("${app.frontend.base-url:http://localhost:3000}")
        private String frontendBaseUrl;

        private static final String ORDER_PARTNER_POST_PREFIX = "POST-";

        private String buildFrontendReturnUrl(String returnPath) {
                String feBase = (frontendBaseUrl == null || frontendBaseUrl.isBlank()) ? "http://localhost:3000" : frontendBaseUrl;
                String path = (returnPath == null || returnPath.isBlank()) ? "/partner/my-listings" : returnPath;
                if (!path.startsWith("/")) path = "/" + path;
                return feBase + path;
        }

        private String buildBackendMomoReturnUrl(String feReturnUrl) {
                return ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/momo/return")
                        .queryParam("returnUrl", feReturnUrl)
                        .build()
                        .toUriString();
        }

        /**
         * Lấy danh sách tin đăng của partner hiện tại (đang đăng nhập)
         */
        @GetMapping("/my-posts")
        @PreAuthorize("hasRole('PARTNER')")
        public ResponseEntity<ApiResponseDto<List<PartnerPostResponse>>> getMyPosts() {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Partners partner = partnerRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("Partner", "username", username));

                List<PartnerPost> posts = partnerPostService.findPostsByPartnerId(partner.getId());
                List<PartnerPostResponse> responses = posts.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponseDto.success(200, "Lấy danh sách tin đăng thành công", responses));
        }

        /**
         * Lấy danh sách tin đăng của partner hiện tại (có phân trang)
         */
        @GetMapping("/my-posts/paged")
        @PreAuthorize("hasRole('PARTNER')")
        public ResponseEntity<ApiResponseDto<Page<PartnerPostResponse>>> getMyPostsPaged(Pageable pageable) {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Partners partner = partnerRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("Partner", "username", username));

                Page<PartnerPost> page = partnerPostService.findPostsByPartnerId(partner.getId(), pageable);
                Page<PartnerPostResponse> response = page.map(this::mapToResponse);
                return ResponseEntity.ok(ApiResponseDto.success(200, "Lấy danh sách tin đăng (phân trang) thành công",
                                response));
        }

        /**
         * Lấy thống kê lượt xem theo tháng (6 tháng gần nhất)
         */
        @GetMapping("/my-posts/stats/monthly-views")
        @PreAuthorize("hasRole('PARTNER')")
        public ResponseEntity<ApiResponseDto<java.util.List<java.util.Map<String, Object>>>> getMonthlyViews() {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Partners partner = partnerRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("Partner", "username", username));

                List<PartnerPost> posts = partnerPostService.findPostsByPartnerId(partner.getId());

                // Group views by month based on createdAt
                java.util.Map<String, Integer> monthlyViews = new java.util.LinkedHashMap<>();

                // Initialize last 6 months
                for (int i = 5; i >= 0; i--) {
                        java.time.YearMonth ym = java.time.YearMonth.now().minusMonths(i);
                        monthlyViews.put(ym.getMonthValue() + "", 0);
                }

                // Aggregate views by month
                for (PartnerPost post : posts) {
                        if (post.getCreatedAt() != null) {
                                String month = String.valueOf(post.getCreatedAt().getMonthValue());
                                int currentViews = monthlyViews.getOrDefault(month, 0);
                                monthlyViews.put(month, currentViews + (post.getViews() != null ? post.getViews() : 0));
                        }
                }

                // Convert to response format with month names
                String[] monthNames = { "", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12" };
                java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();

                for (int i = 5; i >= 0; i--) {
                        java.time.YearMonth ym = java.time.YearMonth.now().minusMonths(i);
                        java.util.Map<String, Object> monthData = new java.util.HashMap<>();
                        monthData.put("month", monthNames[ym.getMonthValue()]);
                        monthData.put("views", monthlyViews.getOrDefault(String.valueOf(ym.getMonthValue()), 0));
                        result.add(monthData);
                }

                return ResponseEntity
                                .ok(ApiResponseDto.success(200, "Lấy thống kê lượt xem theo tháng thành công", result));
        }

        /**
         * Tạo tin đăng mới với ảnh (tối đa 5 ảnh)
         */
        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('PARTNER')")
        public ResponseEntity<ApiResponseDto<PartnerPostResponse>> createPost(
                        @RequestPart("data") String requestData,
                        @RequestPart(value = "images", required = false) MultipartFile[] images) {

                try {
                        // Parse JSON request data
                        PartnerPostCreateRequest request = objectMapper.readValue(requestData,
                                        PartnerPostCreateRequest.class);

                        // Validate images count (max 5)
                        if (images != null && images.length > 5) {
                                return ResponseEntity.badRequest()
                                                .body(ApiResponseDto.error(400, "Chỉ được tải lên tối đa 5 ảnh", null));
                        }

                        String username = SecurityContextHolder.getContext().getAuthentication().getName();
                        Partners partner = partnerRepository.findByUsername(username)
                                        .orElseThrow(() -> new ResourceNotFoundException("Partner", "username",
                                                        username));

                        String orderId = ORDER_PARTNER_POST_PREFIX + UUID.randomUUID();

                        long amount;
                        switch (request.getPostType()) {
                                case NORMAL:
                                        amount = 20000;
                                        break;
                                case VIP1:
                                        amount = 50000;
                                        break;
                                case VIP2:
                                        amount = 100000;
                                        break;
                                case VIP3:
                                        amount = 200000;
                                        break;
                                default:
                                        amount = 20000;
                                        break;
                        }
                        System.out.println("amount: " + amount);

                        String feReturnUrl = buildFrontendReturnUrl("/partner/my-listings");
                        String redirectUrl = buildBackendMomoReturnUrl(feReturnUrl);
                        String orderInfo = "Thanh toan tin dang doi tac";
                        String extraData = "";

                        var momoResp = momoService.createATMPayment(amount, orderId, orderInfo, redirectUrl, extraData);
                        if (momoResp == null || momoResp.getPayUrl() == null || momoResp.getPayUrl().isBlank()) {
                                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                                .body(ApiResponseDto.error(502, "Không thể khởi tạo thanh toán MoMo", null));
                        }
                        String paymentUrl = momoResp.getPayUrl();

                        PartnerPost post = PartnerPost.builder()
                                        .partner(partner)
                                        .title(request.getTitle())
                                        .description(request.getDescription())
                                        .price(request.getPrice())
                                        .area(request.getArea())
                                        .address(request.getAddress())
                                        .postType(request.getPostType())
                                        .orderId(orderId)
                                        .paymentUrl(paymentUrl)
                                        .build();

                        PartnerPost savedPost = partnerPostService.createPost(post);

                        // Save images if provided
                        if (images != null && images.length > 0) {
                                for (int i = 0; i < images.length; i++) {
                                        MultipartFile image = images[i];
                                        if (!image.isEmpty()) {
                                                String filename = fileStorageService.storeFile(image, "partner-posts");
                                                PostImage postImage = PostImage.builder()
                                                                .post(savedPost)
                                                                .imageUrl("/uploads/partner-posts/" + filename)
                                                                .isThumbnail(i == 0) // First image is thumbnail
                                                                .build();
                                                postImageRepository.save(postImage);
                                        }
                                }
                        }

                        PartnerPostResponse response = mapToResponse(savedPost);
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponseDto.success(201,
                                                        "Tạo tin đăng thành công. Tin đăng đang chờ duyệt.", response));
                } catch (Exception e) {
                        log.error("Error creating post with images", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponseDto.error(500, "Lỗi khi tạo tin đăng: " + e.getMessage(),
                                                        null));
                }
        }

        /**
         * Khởi tạo lại link thanh toán MoMo cho tin đang PENDING_PAYMENT (no-ngrok friendly).
         */
        @PostMapping("/{id}/momo/initiate")
        @PreAuthorize("hasRole('PARTNER')")
        public ResponseEntity<ApiResponseDto<java.util.Map<String, String>>> initiatePostMomo(
                        @PathVariable Long id,
                        @RequestParam(required = false) String returnPath) {

                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Partners partner = partnerRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("Partner", "username", username));

                PartnerPost post = partnerPostService.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("PartnerPost", "id", id));

                if (!post.getPartner().getId().equals(partner.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiResponseDto.error(403, "Bạn không có quyền thanh toán cho tin đăng này", null));
                }

                if (post.getStatus() == null || post.getStatus() != PostApprovalStatus.PENDING_PAYMENT) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponseDto.error(400, "Tin đăng không ở trạng thái chờ thanh toán", null));
                }

                long amount = getPostTypePrice(post.getPostType());

                String orderId = ORDER_PARTNER_POST_PREFIX + UUID.randomUUID();
                String feReturnUrl = buildFrontendReturnUrl(returnPath != null ? returnPath : "/partner/my-listings");
                String redirectUrl = buildBackendMomoReturnUrl(feReturnUrl);

                var momoResp = momoService.createATMPayment(amount, orderId,
                                "Thanh toan tin dang #" + id,
                                redirectUrl,
                                "");
                if (momoResp == null || momoResp.getPayUrl() == null || momoResp.getPayUrl().isBlank()) {
                        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                        .body(ApiResponseDto.error(502, "Không thể khởi tạo thanh toán MoMo", null));
                }

                post.setOrderId(orderId);
                post.setPaymentUrl(momoResp.getPayUrl());
                partnerPostService.savePost(post);

                java.util.Map<String, String> resp = new java.util.HashMap<>();
                resp.put("payUrl", momoResp.getPayUrl());
                resp.put("orderId", orderId);
                return ResponseEntity.ok(ApiResponseDto.success(200, "MoMo initiated", resp));
        }

        /**
         * Lấy thông tin chi tiết một tin đăng
         */
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('PARTNER', 'ADMIN', 'MANAGER')")
        public ResponseEntity<ApiResponseDto<PartnerPostResponse>> getPostById(@PathVariable Long id) {
                PartnerPost post = partnerPostService.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("PartnerPost", "id", id));

                // Verify ownership if PARTNER role
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_PARTNER"))) {
                        Partners partner = partnerRepository.findByUsername(username)
                                        .orElseThrow(() -> new ResourceNotFoundException("Partner", "username",
                                                        username));
                        if (!post.getPartner().getId().equals(partner.getId())) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                                .body(ApiResponseDto.error(403, "Bạn không có quyền xem tin đăng này",
                                                                null));
                        }
                }

                PartnerPostResponse response = mapToResponse(post);
                return ResponseEntity.ok(ApiResponseDto.success(200, "Lấy thông tin tin đăng thành công", response));
        }

        /**
         * Cập nhật tin đăng (chỉ khi PENDING hoặc REJECTED) với ảnh mới
         */
        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('PARTNER')")
        public ResponseEntity<ApiResponseDto<PartnerPostResponse>> updatePost(
                        @PathVariable Long id,
                        @RequestPart("data") String requestData,
                        @RequestPart(value = "images", required = false) MultipartFile[] images) {

                try {
                        PartnerPostCreateRequest request = objectMapper.readValue(requestData,
                                        PartnerPostCreateRequest.class);

                        // Validate images count (max 5)
                        if (images != null && images.length > 5) {
                                return ResponseEntity.badRequest()
                                                .body(ApiResponseDto.error(400, "Chỉ được tải lên tối đa 5 ảnh", null));
                        }

                        String username = SecurityContextHolder.getContext().getAuthentication().getName();
                        Partners partner = partnerRepository.findByUsername(username)
                                        .orElseThrow(() -> new ResourceNotFoundException("Partner", "username",
                                                        username));

                        PartnerPost post = partnerPostService.findById(id)
                                        .orElseThrow(() -> new ResourceNotFoundException("PartnerPost", "id", id));

                        // Verify ownership
                        if (!post.getPartner().getId().equals(partner.getId())) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                                .body(ApiResponseDto.error(403, "Bạn không có quyền sửa tin đăng này",
                                                                null));
                        }

                        // Only allow edit when not approved
                        if (post.getStatus() == PostApprovalStatus.APPROVED) {
                                return ResponseEntity.badRequest()
                                                .body(ApiResponseDto.error(400, "Không thể sửa tin đã được duyệt",
                                                                null));
                        }

                        // Keep original package on update.
                        // Business rule: partner cannot change post type after creating the post.
                        PostType oldPostType = post.getPostType();

                        post.setTitle(request.getTitle());
                        post.setDescription(request.getDescription());
                        post.setPrice(request.getPrice());
                        post.setArea(request.getArea());
                        post.setAddress(request.getAddress());
                        post.setPostType(oldPostType);

                        PartnerPost updated = partnerPostService.updatePost(post);

                        // Update images if provided
                        if (images != null && images.length > 0) {
                                // Delete old images
                                List<PostImage> oldImages = postImageRepository.findByPostId(id);
                                postImageRepository.deleteAll(oldImages);

                                // Save new images
                                for (int i = 0; i < images.length; i++) {
                                        MultipartFile image = images[i];
                                        if (!image.isEmpty()) {
                                                String filename = fileStorageService.storeFile(image, "partner-posts");
                                                PostImage postImage = PostImage.builder()
                                                                .post(updated)
                                                                .imageUrl("/uploads/partner-posts/" + filename)
                                                                .isThumbnail(i == 0)
                                                                .build();
                                                postImageRepository.save(postImage);
                                        }
                                }
                        }

                        PartnerPostResponse response = mapToResponse(updated);
                        return ResponseEntity.ok(ApiResponseDto.success(200, "Cập nhật tin đăng thành công", response));
                } catch (Exception e) {
                        log.error("Error updating post with images", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiResponseDto.error(500, "Lỗi khi cập nhật tin đăng: " + e.getMessage(),
                                                        null));
                }
        }

        /**
         * Helper method to get price of PostType in VND
         */
        private long getPostTypePrice(PostType postType) {
                switch (postType) {
                        case NORMAL:
                                return 20000; // 2.000đ/ngày * 50 ngày
                        case VIP1:
                                return 50000; // 5.000đ/ngày * 40 ngày
                        case VIP2:
                                return 100000; // 15.000đ/ngày * 20 ngày
                        case VIP3:
                                return 200000; // 50.000đ/ngày * 8 ngày
                        default:
                                return 20000;
                }
        }

        /**
         * Xóa tin đăng (chỉ khi PENDING hoặc REJECTED)
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('PARTNER')")
        public ResponseEntity<ApiResponseDto<Void>> deletePost(@PathVariable Long id) {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Partners partner = partnerRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("Partner", "username", username));

                PartnerPost post = partnerPostService.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("PartnerPost", "id", id));

                // Verify ownership
                if (!post.getPartner().getId().equals(partner.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiResponseDto.error(403, "Bạn không có quyền xóa tin đăng này", null));
                }

                // Only allow delete if PENDING or REJECTED
                if (post.getStatus() == PostApprovalStatus.APPROVED) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponseDto.error(400,
                                                        "Không thể xóa tin đã được duyệt. Vui lòng liên hệ quản trị viên.",
                                                        null));
                }

                partnerPostService.deletePost(id);

                return ResponseEntity.ok(ApiResponseDto.success(200, "Xóa tin đăng thành công", null));
        }

        // Helper method to map entity to response DTO
        private PartnerPostResponse mapToResponse(PartnerPost post) {
                List<String> imageUrls = postImageRepository.findByPostId(post.getId())
                                .stream()
                                .map(PostImage::getImageUrl)
                                .collect(Collectors.toList());

                return PartnerPostResponse.builder()
                                .id(post.getId())
                                .title(post.getTitle())
                                .description(post.getDescription())
                                .price(post.getPrice())
                                .area(post.getArea())
                                .address(post.getAddress())
                                .postType(post.getPostType())
                                .status(post.getStatus())
                                .createdAt(post.getCreatedAt())
                                .approvedAt(post.getApprovedAt())
                                .approvedByName(post.getApprovedBy() != null ? post.getApprovedBy().getFullName()
                                                : null)
                                .partnerId(post.getPartner().getId())
                                .partnerName(post.getPartner().getCompanyName())
                                .partnerPhone(post.getPartner().getPhoneNumber())
                                .rejectReason(post.getRejectReason())
                                .rejectCount(post.getRejectCount())
                                .updateCount(post.getUpdateCount())
                                .updatedAfterReject(post.isUpdatedAfterReject())
                                .lastRejectedAt(post.getLastRejectedAt())
                                .lastResubmittedAt(post.getLastResubmittedAt())
                                .paymentUrl(post.getPaymentUrl())
                                .views(post.getViews())
                                .imageUrls(imageUrls)
                                .build();
        }
}
