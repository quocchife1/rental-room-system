package com.example.rental.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hóa đơn thuộc về hợp đồng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount; // tổng tiền

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    // Used for monthly grouping/uniqueness (e.g., 2025-12)
    @Column(name = "billing_year")
    private Integer billingYear;

    @Column(name = "billing_month")
    private Integer billingMonth;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    // Cash/direct vs online (for accounting traceability)
    @Column(name = "paid_direct")
    private Boolean paidDirect;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private InvoiceStatus status;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceDetail> details;
}
