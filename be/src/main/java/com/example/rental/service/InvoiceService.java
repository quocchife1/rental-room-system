package com.example.rental.service;

import com.example.rental.dto.invoice.InvoiceRequest;
import com.example.rental.dto.invoice.InvoiceResponse;
import com.example.rental.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InvoiceService {
    InvoiceResponse create(InvoiceRequest request);
    InvoiceResponse getById(Long id);
    List<InvoiceResponse> getAll();
    Page<InvoiceResponse> getAll(Pageable pageable);
    Page<InvoiceResponse> search(Pageable pageable, Integer year, Integer month, InvoiceStatus status);
    InvoiceResponse markPaid(Long id, boolean direct);
    void sendReminderForInvoice(Long id);
    void markOverdueAndNotify(Long id);

    // Thêm mới để scheduler gọi
    void checkAndSendDueReminders();

    // Lấy hóa đơn dành cho 1 người thuê (tenant)
    java.util.List<com.example.rental.dto.invoice.InvoiceResponse> getInvoicesForTenant(Long tenantId);
    org.springframework.data.domain.Page<com.example.rental.dto.invoice.InvoiceResponse> getInvoicesForTenant(Long tenantId, org.springframework.data.domain.Pageable pageable);

    com.example.rental.dto.invoice.MonthlyInvoiceGenerateResponse generateMonthlyInvoices(com.example.rental.dto.invoice.MonthlyInvoiceGenerateRequest request);

    java.util.List<com.example.rental.dto.invoice.ContractMonthlyInvoicePreviewResponse> previewMonthlyInvoices(int year, int month);

    com.example.rental.dto.invoice.InvoiceResponse generateMonthlyInvoiceForContract(Long contractId, com.example.rental.dto.invoice.MonthlyInvoiceGenerateRequest request);

    /**
     * Tạo hóa đơn phát sinh (không bị ràng buộc 1 hóa đơn/tháng) – dùng cho các trường hợp như bảo trì do lỗi người thuê.
     */
    InvoiceResponse createMaintenanceInvoice(Long contractId, java.time.LocalDate dueDate, java.math.BigDecimal amount, String note);
}
