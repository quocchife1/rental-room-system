package com.example.rental.dto.damage;

import lombok.Data;

/**
 * DTO cho hình ảnh chứng minh hư hỏng
 */
@Data
public class DamageImageDto {
    private Long id;
    private String imageUrl;
    private String description;
}
