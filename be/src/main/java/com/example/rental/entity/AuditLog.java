package com.example.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Nhật ký kiểm toán (Audit Log)
 * Ghi lại tất cả các thay đổi quan trọng về tài chính, hợp đồng, hệ thống
 * Dữ liệu log KHÔNG được phép xóa hoặc sửa (Write-once)
 * Chỉ Giám đốc/Admin được xem
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_actor", columnList = "actor_id"),
    @Index(name = "idx_action", columnList = "action"),
    @Index(name = "idx_target", columnList = "target_type,target_id"),
    @Index(name = "idx_timestamp", columnList = "created_at")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Người thực hiện hành động
     * Format: "UserID:Username (Role)" hoặc "System"
     */
    @Column(name = "actor_id", nullable = false, length = 100)
    private String actorId;

    /**
     * Tên vai trò của người thực hiện: ADMIN, MANAGER, ACCOUNTANT, RECEPTIONIST, MAINTENANCE
     */
    @Column(name = "actor_role", length = 50)
    private String actorRole;

    /**
     * Hành động được thực hiện: CREATE, UPDATE, DELETE, APPROVE, REJECT, CONFIRM_PAYMENT, etc.
     */
    @Column(name = "action", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    /**
     * Loại đối tượng bị ảnh hưởng: CONTRACT, INVOICE, TENANT, ROOM, etc.
     */
    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;

    /**
     * ID của đối tượng bị ảnh hưởng
     */
    @Column(name = "target_id")
    private Long targetId;

    /**
     * Mô tả hành động (tuỳ chọn)
     */
    @Column(length = 500)
    private String description;

    /**
     * Giá trị cũ (JSON format) - Để so sánh với giá trị mới
     */
    @Lob
    @Column(name = "old_value")
    private String oldValue;

    /**
     * Giá trị mới (JSON format)
     */
    @Lob
    @Column(name = "new_value")
    private String newValue;

    /**
     * Địa chỉ IP của người thực hiện hành động
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User-Agent của client
     */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /**
     * Chi nhánh liên quan (nếu có)
     */
    @Column(name = "branch_id")
    private Long branchId;

    /**
     * Thời gian thực hiện (tự động set, không thể sửa)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Trạng thái log: SUCCESS, FAILURE
     */
    @Column(name = "status", length = 20)
    private String status; // SUCCESS, FAILURE

    /**
     * Thông báo lỗi (nếu status = FAILURE)
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;
}
