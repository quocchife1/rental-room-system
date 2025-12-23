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
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ với người thuê
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    // Quan hệ với phòng (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Denormalized snapshot (lưu branch + roomNumber tại thời điểm tạo hợp đồng)
    @Column(name = "branch_code", length = 10, nullable = false)
    private String branchCode;

    @Column(name = "room_number", length = 100, nullable = false)
    private String roomNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal deposit;

    // --- Deposit payment tracking (after signed upload, before ACTIVE) ---
    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_payment_method", length = 30)
    private PaymentMethod depositPaymentMethod;

    @Column(name = "deposit_paid_date")
    private LocalDateTime depositPaidDate;

    @Column(name = "deposit_payment_reference", length = 255)
    private String depositPaymentReference;

    @Column(name = "deposit_invoice_url", length = 255)
    private String depositInvoiceUrl;

    @Column(name = "deposit_receipt_url", length = 255)
    private String depositReceiptUrl;

    // Email reminder for checkout near contract end
    @Column(name = "end_reminder_sent", nullable = false)
    private boolean endReminderSent;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ContractStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // File hợp đồng mẫu PDF sinh tự động (snapshot link)
    @Column(length = 255)
    private String contractFileUrl;

    // File hợp đồng đã ký, lưu trên BE (URL)
    @Column(length = 255)
    private String signedContractUrl;

    // ✅ Quan hệ: hợp đồng có nhiều dịch vụ
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractService> services;
}
