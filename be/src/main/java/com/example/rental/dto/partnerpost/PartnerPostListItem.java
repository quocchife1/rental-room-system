package com.example.rental.dto.partnerpost;

import com.example.rental.entity.PostApprovalStatus;
import com.example.rental.entity.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PartnerPostListItem {
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
}
