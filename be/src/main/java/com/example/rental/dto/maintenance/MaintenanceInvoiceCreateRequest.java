package com.example.rental.dto.maintenance;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MaintenanceInvoiceCreateRequest {
    /** Tổng tiền cần thu (do lỗi người thuê). Nếu null sẽ dùng cost trong request. */
    private BigDecimal amount;

    /** Hạn thanh toán (nếu null, backend sẽ default). */
    private LocalDate dueDate;

    /** Ghi chú hiển thị trên hóa đơn (optional). */
    private String note;
}
