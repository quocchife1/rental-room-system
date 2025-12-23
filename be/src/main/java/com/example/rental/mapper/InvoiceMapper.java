package com.example.rental.mapper;

import com.example.rental.dto.invoice.*;
import com.example.rental.entity.Invoice;
import com.example.rental.entity.InvoiceDetail;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceMapper {

    public static InvoiceDetail toDetailEntity(InvoiceDetailRequest req, Invoice invoice) {
        int qty = req.getQuantity() != null ? req.getQuantity() : 1;
        BigDecimal unitPrice = req.getUnitPrice() != null ? req.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(qty));

        return InvoiceDetail.builder()
                .invoice(invoice)
                .description(req.getDescription())
                .unitPrice(unitPrice)
                .quantity(qty)
                .amount(total)
                .build();
    }

    public static InvoiceDetailResponse toDetailResponse(InvoiceDetail d) {
        return InvoiceDetailResponse.builder()
                .id(d.getId())
                .description(d.getDescription())
                .unitPrice(d.getUnitPrice())
                .quantity(d.getQuantity())
                .amount(d.getAmount())
                .build();
    }

    public static InvoiceResponse toResponse(Invoice inv) {
        List<InvoiceDetailResponse> details = inv.getDetails() != null
                ? inv.getDetails().stream().map(InvoiceMapper::toDetailResponse).collect(Collectors.toList())
                : List.of();

        var contract = inv.getContract();
        var tenant = contract != null ? contract.getTenant() : null;
        var room = contract != null ? contract.getRoom() : null;
        var branch = room != null ? room.getBranch() : null;

        return InvoiceResponse.builder()
                .id(inv.getId())
                .contractId(contract != null ? contract.getId() : null)
                .tenantId(tenant != null ? tenant.getId() : null)
                .tenantUsername(tenant != null ? tenant.getUsername() : null)
                .tenantFullName(tenant != null ? tenant.getFullName() : null)
                .tenantEmail(tenant != null ? tenant.getEmail() : null)
                .tenantPhoneNumber(tenant != null ? tenant.getPhoneNumber() : null)
                .tenantCccd(tenant != null ? tenant.getCccd() : null)
                .tenantStudentId(tenant != null ? tenant.getStudentId() : null)
                .tenantUniversity(tenant != null ? tenant.getUniversity() : null)
                .branchId(branch != null ? branch.getId() : null)
                .branchCode(contract != null ? contract.getBranchCode() : (room != null ? room.getBranchCode() : null))
                .branchName(branch != null ? branch.getBranchName() : null)
                .roomId(room != null ? room.getId() : null)
                .roomCode(room != null ? room.getRoomCode() : null)
                .roomNumber(contract != null ? contract.getRoomNumber() : (room != null ? room.getRoomNumber() : null))
                .amount(inv.getAmount())
                .dueDate(inv.getDueDate())
            .billingYear(inv.getBillingYear())
            .billingMonth(inv.getBillingMonth())
                .paidDate(inv.getPaidDate())
            .paidDirect(inv.getPaidDirect())
            .paymentReference(inv.getPaymentReference())
                .status(inv.getStatus() != null ? inv.getStatus().name() : null)
                .createdAt(inv.getCreatedAt())
                .details(details)
                .build();
    }
}
