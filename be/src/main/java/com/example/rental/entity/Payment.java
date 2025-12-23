package com.example.rental.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 50)
    private String method;

    @Column(length = 100)
    private String providerRef;

    @Column(length = 20)
    private String status; // PENDING, SUCCESS, FAILED

    @Column(length = 100)
    private String processedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
