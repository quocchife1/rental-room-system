package com.example.rental.controller;

import com.example.rental.dto.payment.PaymentRequest;
import com.example.rental.dto.payment.PaymentResponse;
import com.example.rental.dto.ApiResponseDto;
import com.example.rental.service.PaymentService;
import com.example.rental.service.InvoiceService;
import com.example.rental.service.AuditLogService;
import com.example.rental.entity.AuditAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment (stub)", description = "Payment stub endpoints for testing")
public class PaymentController {

    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final AuditLogService auditLogService;
    private final com.example.rental.repository.PaymentRepository paymentRepository;

    @Operation(summary = "Process payment (stub)")
    @PostMapping("/pay")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<PaymentResponse>> pay(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse resp = paymentService.processPayment(request);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Payment processed", resp));
    }

    @Operation(summary = "Initiate MoMo payment (demo)")
    @PostMapping("/momo/initiate")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.Map<String, String>>> initiateMoMo(@RequestBody PaymentRequest request, @RequestParam(required = false) String returnUrl) {
        String redirect = paymentService.initiateMoMo(request, returnUrl);
        java.util.Map<String,String> resp = java.util.Collections.singletonMap("redirectUrl", redirect);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "MoMo initiated", resp));
    }

    @Operation(summary = "MoMo provider callback (demo)")
    @GetMapping("/momo/callback")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<String>> momoCallback(@RequestParam Long invoiceId, @RequestParam String providerRef, @RequestParam String status, @RequestParam(required = false) String returnUrl) {
        // provider would call this after user pays; for demo we'll accept status=SUCCESS to mark paid
        if("SUCCESS".equalsIgnoreCase(status)){
            invoiceService.markPaid(invoiceId, true);
            try{
                auditLogService.logAction("SYSTEM","SYSTEM", AuditAction.CONFIRM_PAYMENT, "INVOICE", invoiceId, "MoMo callback - payment confirmed", null, "{\"providerRef\":\""+providerRef+"\"}", "momo-provider", null, "momo-callback", "SUCCESS", null);
                // update payment record status if exists
                try{
                    java.util.Optional<com.example.rental.entity.Payment> opt = paymentRepository.findAll().stream().filter(p-> providerRef.equals(p.getProviderRef()) || (p.getInvoiceId()!=null && p.getInvoiceId().equals(invoiceId))).findFirst();
                    if(opt.isPresent()){
                        com.example.rental.entity.Payment pay = opt.get();
                        pay.setStatus("SUCCESS");
                        paymentRepository.save(pay);
                    }
                }catch(Exception ex2){ }
            }catch(Exception ex){ }
            // redirect frontend
            String target = (returnUrl != null && !returnUrl.isEmpty()) ? returnUrl : "/receipt?invoiceId="+invoiceId;
            return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Payment confirmed", target));
        }else{
            return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.error(400, "Payment failed", "PROVIDER_FAILED", null));
        }
    }

    @Operation(summary = "Fake MoMo redirect page for demo (returns callback link)")
    @GetMapping("/momo/fake-redirect")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.Map<String,String>>> momoFakeRedirect(@RequestParam Long invoiceId, @RequestParam String providerRef, @RequestParam(required = false) String returnUrl) {
        // For demo: mark invoice paid and redirect to frontend receipt page
        try{
            invoiceService.markPaid(invoiceId, true);
            auditLogService.logAction("SYSTEM","SYSTEM", AuditAction.CONFIRM_PAYMENT, "INVOICE", invoiceId, "MoMo fake-redirect - marked paid", null, "{\"providerRef\":\""+providerRef+"\"}", "momo-fake", null, "momo-fake-redirect", "SUCCESS", null);
        }catch(Exception ex){ }
        String target = (returnUrl != null && !returnUrl.isEmpty()) ? returnUrl : "/receipt?invoiceId="+invoiceId;
        java.net.URI uri = java.net.URI.create(target);
        return ResponseEntity.status(302).location(uri).body(com.example.rental.dto.ApiResponseDto.success(302, "Redirecting", java.util.Collections.singletonMap("location", target)));
    }

    @GetMapping("/paged")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<org.springframework.data.domain.Page<com.example.rental.entity.Payment>>> getPaymentsPaged(org.springframework.data.domain.Pageable pageable){
        org.springframework.data.domain.Page<com.example.rental.entity.Payment> page = paymentRepository.findAll(pageable);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Payments page fetched", page));
    }
}
