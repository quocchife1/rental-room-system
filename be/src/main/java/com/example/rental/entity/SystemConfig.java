package com.example.rental.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    private Long id;

    @Column(name = "electric_price_per_unit", precision = 12, scale = 2)
    private BigDecimal electricPricePerUnit;

    @Column(name = "water_price_per_unit", precision = 12, scale = 2)
    private BigDecimal waterPricePerUnit;

    @Column(name = "late_fee_per_day", precision = 12, scale = 2)
    private BigDecimal lateFeePerDay;

    // --- Bank transfer receiver info (MoMo) ---
    @Column(name = "momo_receiver_name", length = 120)
    private String momoReceiverName;

    @Column(name = "momo_receiver_phone", length = 30)
    private String momoReceiverPhone;

    @Column(name = "momo_receiver_qr_url", length = 255)
    private String momoReceiverQrUrl;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
