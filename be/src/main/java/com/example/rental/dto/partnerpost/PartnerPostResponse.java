package com.example.rental.dto.partnerpost;

import com.example.rental.entity.PostApprovalStatus;
import com.example.rental.entity.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerPostResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal area;
    private String address;
    private PostType postType;
    private PostApprovalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String approvedByName;
    private Long partnerId;
    private String partnerName;
    private String partnerPhone;
    private String rejectReason;
    private String paymentUrl;
    private Integer views;
    private List<String> imageUrls; // URLs của ảnh
}
