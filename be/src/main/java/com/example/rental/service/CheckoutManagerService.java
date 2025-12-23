package com.example.rental.service;

import com.example.rental.dto.checkout.CheckoutRequestManagerRow;
import com.example.rental.dto.damage.DamageReportCreateRequest;
import com.example.rental.dto.damage.DamageReportResponse;
import com.example.rental.dto.invoice.InvoiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface CheckoutManagerService {
    Page<CheckoutRequestManagerRow> listMyBranchRequests(List<String> statuses, Pageable pageable);

    CheckoutRequestManagerRow approve(Long requestId);

    DamageReportResponse getOrCreateInspectionReport(Long requestId);

    DamageReportResponse saveInspectionReport(Long requestId, DamageReportCreateRequest request);

    DamageReportResponse uploadItemImages(Long requestId, String itemKey, List<MultipartFile> images) throws IOException;

    InvoiceResponse createSettlementInvoice(Long requestId, LocalDate dueDate);
}
