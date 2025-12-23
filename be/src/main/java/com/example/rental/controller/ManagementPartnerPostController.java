package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.partnerpost.PartnerPostResponse;
import com.example.rental.dto.partnerpost.PartnerPostListItem;
import com.example.rental.entity.PartnerPost;
import com.example.rental.entity.PostImage;
import com.example.rental.repository.PartnerPostRepository;
import com.example.rental.repository.EmployeeRepository;
import com.example.rental.repository.PostImageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/management/partner-posts")
public class ManagementPartnerPostController {

    private final PartnerPostRepository partnerPostRepository;
    private final EmployeeRepository employeeRepository;
    private final PostImageRepository postImageRepository;

    public ManagementPartnerPostController(PartnerPostRepository partnerPostRepository,
            EmployeeRepository employeeRepository,
            PostImageRepository postImageRepository) {
        this.partnerPostRepository = partnerPostRepository;
        this.employeeRepository = employeeRepository;
        this.postImageRepository = postImageRepository;
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','RECEPTIONIST')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseDto<Page<PartnerPostResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        List<com.example.rental.entity.PostApprovalStatus> statuses = normalizeStatuses(status);
        String keyword = q == null ? "" : q.trim();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PartnerPostListItem> result = partnerPostRepository
                .pageListItems(statuses, keyword, pageable);
        Page<PartnerPostResponse> resp = result.map(item -> PartnerPostResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .price(item.getPrice())
                .area(item.getArea())
                .address(item.getAddress())
                .postType(item.getPostType())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .approvedAt(item.getApprovedAt())
                .approvedByName(item.getApprovedByName())
                .partnerId(item.getPartnerId())
                .partnerName(item.getPartnerName())
                .partnerPhone(item.getPartnerPhone())
                .imageUrls(java.util.List.of())
                .rejectReason(item.getRejectReason())
                .build());
        return ResponseEntity.ok(ApiResponseDto.success(200, "Danh sách tin theo bộ lọc", resp));
    }

    private List<com.example.rental.entity.PostApprovalStatus> normalizeStatuses(String raw) {
        if (raw == null || raw.isBlank()) {
            return java.util.Arrays.asList(
                    com.example.rental.entity.PostApprovalStatus.PENDING_APPROVAL,
                    com.example.rental.entity.PostApprovalStatus.APPROVED,
                    com.example.rental.entity.PostApprovalStatus.REJECTED);
        }
        String norm = raw.trim().toUpperCase();
        if ("PENDING".equals(norm))
            norm = "PENDING_APPROVAL";
        return java.util.List.of(com.example.rental.entity.PostApprovalStatus.valueOf(norm));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','RECEPTIONIST')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseDto<Page<PartnerPostResponse>>> listPending(Pageable pageable) {
        Page<PartnerPost> page = partnerPostRepository.findByStatusInAndIsDeletedFalse(
                java.util.List.of(com.example.rental.entity.PostApprovalStatus.PENDING_APPROVAL), pageable);
        Page<PartnerPostResponse> resp = page.map(this::mapToResponse);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Danh sách tin chờ duyệt", resp));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','RECEPTIONIST')")
    public ResponseEntity<ApiResponseDto<PartnerPostResponse>> getById(@PathVariable Long id) {
        PartnerPost post = partnerPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin"));
        return ResponseEntity.ok(ApiResponseDto.success(200, "Lấy chi tiết tin thành công", mapToResponse(post)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','RECEPTIONIST')")
    public ResponseEntity<ApiResponseDto<Void>> approve(@PathVariable Long id) {
        PartnerPost post = partnerPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin"));
        post.setStatus(com.example.rental.entity.PostApprovalStatus.APPROVED);
        post.setApprovedAt(java.time.LocalDateTime.now());
        // set approvedBy from current authenticated employee if available
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : null;
            if (username != null && username.trim().length() > 0) {
                employeeRepository.findByUsername(username).ifPresent(post::setApprovedBy);
            }
        } catch (Exception ignored) {
        }
        partnerPostRepository.save(post);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Đã duyệt tin", null));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','RECEPTIONIST')")
    public ResponseEntity<ApiResponseDto<Void>> reject(@PathVariable Long id,
            @RequestParam(required = false) String reason) {
        PartnerPost post = partnerPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin"));
        post.setStatus(com.example.rental.entity.PostApprovalStatus.REJECTED);
        post.setRejectReason(reason);
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : null;
            if (username != null && username.trim().length() > 0) {
                employeeRepository.findByUsername(username).ifPresent(post::setApprovedBy);
            }
        } catch (Exception ignored) {
        }
        partnerPostRepository.save(post);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Đã từ chối tin", null));
    }

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
                .approvedByName(post.getApprovedBy() != null ? post.getApprovedBy().getFullName() : null)
                .partnerId(post.getPartner().getId())
                .partnerName(post.getPartner().getCompanyName())
                .partnerPhone(post.getPartner().getPhoneNumber())
                .rejectReason(post.getRejectReason())
                .views(post.getViews())
                .imageUrls(imageUrls)
                .build();
    }

    // Additional fields for response
    private void enrichResponse(PartnerPostResponse resp, PartnerPost post) {
        resp.setCreatedAt(post.getCreatedAt());
        resp.setRejectReason(post.getRejectReason());
        if (post.getApprovedAt() != null) {
            resp.setApprovedAt(post.getApprovedAt());
        }
        if (post.getApprovedBy() != null) {
            resp.setApprovedByName(post.getApprovedBy().getFullName());
        }
        if (post.getPartner() != null) {
            resp.setPartnerName(post.getPartner().getCompanyName());
            resp.setPartnerPhone(post.getPartner().getPhoneNumber());
            resp.setPartnerId(post.getPartner().getId());
        }
    }

    // ===== Bulk operations =====
    public static class IdsRequest {
        public java.util.List<Long> ids;
        public String reason; // optional for reject-batch

        public java.util.List<Long> getIds() {
            return ids;
        }

        public String getReason() {
            return reason;
        }
    }

    @PostMapping("/approve-batch")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','RECEPTIONIST')")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> approveBatch(@RequestBody IdsRequest req) {
        List<Long> ids = req != null && req.ids != null ? req.ids : java.util.List.of();
        int updated = 0;
        if (!ids.isEmpty()) {
            List<PartnerPost> posts = partnerPostRepository.findAllById(ids);
            LocalDateTime now = java.time.LocalDateTime.now();
            for (PartnerPost p : posts) {
                p.setStatus(com.example.rental.entity.PostApprovalStatus.APPROVED);
                p.setApprovedAt(now);
                updated++;
            }
            partnerPostRepository.saveAll(posts);
        }
        Map<String, Object> result = java.util.Map.of("updated", updated, "requested", ids.size());
        return ResponseEntity.ok(ApiResponseDto.success(200, "Đã duyệt hàng loạt", result));
    }

    @PostMapping("/reject-batch")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','RECEPTIONIST')")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> rejectBatch(@RequestBody IdsRequest req) {
        List<Long> ids = req != null && req.ids != null ? req.ids : java.util.List.of();
        String reason = req != null ? req.reason : null;
        int updated = 0;
        if (!ids.isEmpty()) {
            List<PartnerPost> posts = partnerPostRepository.findAllById(ids);
            for (PartnerPost p : posts) {
                p.setStatus(com.example.rental.entity.PostApprovalStatus.REJECTED);
                p.setRejectReason(reason);
                updated++;
            }
            partnerPostRepository.saveAll(posts);
        }
        Map<String, Object> result = java.util.Map.of("updated", updated, "requested", ids.size());
        return ResponseEntity.ok(ApiResponseDto.success(200, "Đã từ chối hàng loạt", result));
    }

    // ===== Stats =====
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','RECEPTIONIST')")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> stats() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        long pending = partnerPostRepository
                .countByStatusAndIsDeletedFalse(com.example.rental.entity.PostApprovalStatus.PENDING_APPROVAL);
        System.out.println("So bai dang PENDING_APPROVAL: " + pending);
        long approved = partnerPostRepository
                .countByStatusAndIsDeletedFalse(com.example.rental.entity.PostApprovalStatus.APPROVED);
        long rejected = partnerPostRepository
                .countByStatusAndIsDeletedFalse(com.example.rental.entity.PostApprovalStatus.REJECTED);

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        long approvedToday = partnerPostRepository.countByApprovedAtBetweenAndIsDeletedFalse(start, end);

        Map<String, Object> result = java.util.Map.of(
                "pending", pending,
                "approved", approved,
                "rejected", rejected,
                "approvedToday", approvedToday);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Thống kê duyệt tin", result));
    }
}
