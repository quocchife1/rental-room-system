package com.example.rental.dto.damage;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Request để tạo/cập nhật báo cáo hư hỏng
 */
@Data
public class DamageReportCreateRequest {

    private Long contractId; // ID hợp đồng cần trả phòng
    private String description; // Mô tả tổng quát tình trạng phòng
    private String damageDetails; // Chi tiết hư hỏng (JSON format)
    private BigDecimal totalDamageCost; // Tổng chi phí sửa chữa
}
