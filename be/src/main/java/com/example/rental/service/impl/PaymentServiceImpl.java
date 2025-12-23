package com.example.rental.service.impl;

import com.example.rental.dto.payment.PaymentRequest;
import com.example.rental.dto.payment.PaymentResponse;
import com.example.rental.entity.AuditAction;
import com.example.rental.service.AuditLogService;
import com.example.rental.service.InvoiceService;
import com.example.rental.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.rental.entity.Payment;
import com.example.rental.repository.PaymentRepository;
import java.util.Optional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final InvoiceService invoiceService;
    private final AuditLogService auditLogService;
    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        // Very small stub: mark invoice as paid and record audit
        var respInvoice = invoiceService.markPaid(request.getInvoiceId(), true);

        PaymentResponse resp = new PaymentResponse();
        resp.setInvoiceId(request.getInvoiceId());
        resp.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
        resp.setMethod(request.getMethod());
        resp.setProviderRef(request.getProviderRef());
        resp.setStatus("SUCCESS");

        // persist payment record
        try{
            Payment p = Payment.builder()
                .invoiceId(request.getInvoiceId())
                .amount(resp.getAmount())
                .method(resp.getMethod())
                .providerRef(resp.getProviderRef())
                .status(resp.getStatus())
                .processedBy("SYSTEM")
                .build();
            Payment saved = paymentRepository.save(p);
            resp.setPaymentId(saved.getId());
        }catch(Exception ex){ /* don't fail payment flow */ }

        // Log audit: CONFIRM_PAYMENT
        try {
            auditLogService.logAction(
                    "SYSTEM",
                    "SYSTEM",
                    AuditAction.CONFIRM_PAYMENT,
                    "INVOICE",
                    request.getInvoiceId(),
                    "Payment processed by stub",
                    null,
                    "{\"amount\":" + resp.getAmount() + "}",
                    "127.0.0.1",
                    null,
                    "payment-stub",
                    "SUCCESS",
                    null
            );
        } catch (Exception ex) {
            // don't fail payment flow if audit logging fails
        }

        return resp;
    }

    @Override
    public String initiateMoMo(PaymentRequest request, String returnUrl) {
        // Create a fake provider redirect URL for demo purposes.
        // In a real integration this would call MoMo APIs and return their checkout URL.
        String providerRef = "momo-" + System.currentTimeMillis();

        // record audit that MoMo initiation was created
        try {
                auditLogService.logAction(
                    "SYSTEM",
                    "SYSTEM",
                    AuditAction.SYSTEM_AUTO_ACTION,
                    "INVOICE",
                    request.getInvoiceId(),
                    "Initiated MoMo payment",
                    null,
                    "{\"providerRef\":\"" + providerRef + "\"}",
                    "127.0.0.1",
                    null,
                    "momo-init",
                    "PENDING",
                    null
                );
        } catch (Exception ex) { }

        // persist a pending payment record
        try{
            Payment p = Payment.builder()
                .invoiceId(request.getInvoiceId())
                .amount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO)
                .method("MOMO")
                .providerRef(providerRef)
                .status("PENDING")
                .processedBy("SYSTEM")
                .build();
            paymentRepository.save(p);
        }catch(Exception e){ }

        // For the demo, redirect to a local stub endpoint that simulates the MoMo page
        String redirect = "/api/payments/momo/fake-redirect?invoiceId=" + request.getInvoiceId() + "&providerRef=" + providerRef + "&returnUrl=" + java.net.URLEncoder.encode(returnUrl == null ? "/billing" : returnUrl, java.nio.charset.StandardCharsets.UTF_8);
        return redirect;
    }
}
