package com.example.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hình ảnh chứng minh hư hỏng phòng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "damage_images")
public class DamageImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Báo cáo hư hỏng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "damage_report_id", nullable = false)
    private DamageReport damageReport;

    /**
     * URL hình ảnh
     */
    @Column(nullable = false, length = 255)
    private String imageUrl;

    /**
     * Mô tả hình ảnh
     */
    @Column(length = 255)
    private String description;
}
