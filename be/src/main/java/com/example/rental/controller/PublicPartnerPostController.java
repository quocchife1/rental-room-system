package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.partnerpost.PartnerPostResponse;
import com.example.rental.entity.PartnerPost;
import com.example.rental.entity.PostImage;
import com.example.rental.repository.PostImageRepository;
import com.example.rental.service.PartnerPostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/partner-posts")
@RequiredArgsConstructor
@Tag(name = "Public Partner Posts", description = "API công khai lấy tin đã duyệt/đang hiển thị")
public class PublicPartnerPostController {

    private final PartnerPostService partnerPostService;
    private final PostImageRepository postImageRepository;

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<PartnerPostResponse>>> listApproved(Pageable pageable) {
        Page<PartnerPost> page = partnerPostService.findPublicVisiblePosts(pageable);
        Page<PartnerPostResponse> response = page.map(this::mapToResponse);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Lấy danh sách tin hiển thị thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<PartnerPostResponse>> getPublicPost(@PathVariable Long id) {
        PartnerPost post = partnerPostService.findById(id)
                .orElseThrow(() -> new com.example.rental.exception.ResourceNotFoundException("PartnerPost", "id", id));
        if (post.isDeleted()) {
            throw new com.example.rental.exception.ResourceNotFoundException("PartnerPost", "id", id);
        }

        // Increment views count (direct save to avoid changing status)
        post.setViews(post.getViews() == null ? 1 : post.getViews() + 1);
        partnerPostService.savePost(post);

        PartnerPostResponse response = mapToResponse(post);
        return ResponseEntity.ok(ApiResponseDto.success(200, "Lấy chi tiết tin hiển thị thành công", response));
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
}
