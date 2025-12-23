package com.example.rental.service.impl;

import com.example.rental.dto.checkout.CheckoutRequestDto;
import com.example.rental.dto.checkout.CheckoutRequestResponse;
import com.example.rental.entity.CheckoutRequest;
import com.example.rental.entity.CheckoutStatus;
import com.example.rental.entity.Contract;
import com.example.rental.entity.Tenant;
import com.example.rental.repository.CheckoutRequestRepository;
import com.example.rental.repository.ContractRepository;
import com.example.rental.repository.TenantRepository;
import com.example.rental.service.CheckoutService;
import com.example.rental.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CheckoutRequestRepository checkoutRequestRepository;
    private final ContractRepository contractRepository;
    private final TenantRepository tenantRepository;
    private final InvoiceService invoiceService;

    @Override
    @Transactional
    public CheckoutRequestResponse submitCheckoutRequest(Long contractId, String username, CheckoutRequestDto request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng"));

        Tenant tenant = tenantRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tenant"));

        CheckoutRequest cr = CheckoutRequest.builder()
                .contract(contract)
                .tenant(tenant)
                .status(CheckoutStatus.PENDING)
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .build();

        CheckoutRequest saved = checkoutRequestRepository.save(cr);

        CheckoutRequestResponse resp = new CheckoutRequestResponse();
        resp.setId(saved.getId());
        resp.setContractId(contract.getId());
        resp.setTenantId(tenant.getId());
        resp.setStatus(saved.getStatus().name());
        resp.setReason(saved.getReason());
        resp.setCreatedAt(saved.getCreatedAt());

        return resp;
    }

    @Override
    @Transactional
    public CheckoutRequestResponse approveRequest(Long requestId, String approverUsername) {
        CheckoutRequest req = checkoutRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu checkout"));

        req.setStatus(CheckoutStatus.APPROVED);
        checkoutRequestRepository.save(req);

        CheckoutRequestResponse resp = new CheckoutRequestResponse();
        resp.setId(req.getId());
        resp.setContractId(req.getContract().getId());
        resp.setTenantId(req.getTenant().getId());
        resp.setStatus(req.getStatus().name());
        resp.setReason(req.getReason());
        resp.setCreatedAt(req.getCreatedAt());
        return resp;
    }

    @Override
    @Transactional
    public void finalizeCheckout(Long contractId, String operatorUsername) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng"));

        // 1. Tạo hoá đơn thanh toán cuối nếu cần
        com.example.rental.dto.invoice.InvoiceRequest invoiceRequest = new com.example.rental.dto.invoice.InvoiceRequest();
        invoiceRequest.setContractId(contract.getId());
        invoiceRequest.setDueDate(java.time.LocalDate.now());
        invoiceService.create(invoiceRequest);

        // 2. Đóng hợp đồng và trả phòng
        contract.setStatus(com.example.rental.entity.ContractStatus.ENDED);
        if (contract.getRoom() != null) {
            contract.getRoom().setStatus(com.example.rental.entity.RoomStatus.AVAILABLE);
        }
        contractRepository.save(contract);
    }
}
