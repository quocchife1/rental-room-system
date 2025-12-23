package com.example.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "partner_posts")
public class PartnerPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // QUAN HỆ: Đối tác đăng tin - Bắt buộc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partners partner;

    @Column(name = "order_id", unique = true)
    private String orderId;

    @Column(name = "payment_url", length = 512)
    private String paymentUrl;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 5, scale = 2)
    private BigDecimal area;

    @Column(nullable = false, length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 20)
    private PostType postType; // Sử dụng Enum PostType (NORMAL/VIP1/VIP2/VIP3)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostApprovalStatus status; // Sử dụng Enum PostApprovalStatus

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // QUAN HỆ: Nhân viên duyệt tin (Có thể NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Employees approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "views", nullable = false)
    @Builder.Default
    private Integer views = 0;
}