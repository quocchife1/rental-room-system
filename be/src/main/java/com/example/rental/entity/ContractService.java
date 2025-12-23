package com.example.rental.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contract_services")
public class ContractService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hợp đồng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    // Dịch vụ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private RentalServiceItem service;

    // Số lượng cố định (xe, internet, vệ sinh…)
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    // Chỉ số đầu kỳ (cho điện, nước)
    private BigDecimal previousReading;

    // Chỉ số cuối kỳ (cho điện, nước)
    private BigDecimal currentReading;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
