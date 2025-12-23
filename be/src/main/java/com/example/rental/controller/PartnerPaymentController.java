package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.partnerpayment.PartnerPaymentCreateRequest;
import com.example.rental.dto.partnerpayment.PartnerPaymentResponse;
import com.example.rental.entity.PartnerPayment;
import com.example.rental.entity.PartnerPost;
import com.example.rental.entity.Partners;
import com.example.rental.entity.PostApprovalStatus;
import com.example.rental.entity.ServicePackage;
import com.example.rental.entity.PaymentMethod;
import com.example.rental.exception.ResourceNotFoundException;
import com.example.rental.repository.PartnerPaymentRepository;
import com.example.rental.repository.PartnerPostRepository;
import com.example.rental.repository.PartnerRepository;
import com.example.rental.repository.ServicePackageRepository;
import com.example.rental.service.util.CodeGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/partner-payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Partner Payments", description = "API thanh toán gói dịch vụ cho đối tác")
public class PartnerPaymentController {

    private final PartnerPaymentRepository partnerPaymentRepository;
    private final PartnerPostRepository partnerPostRepository;
    private final PartnerRepository partnerRepository;
        private final ServicePackageRepository servicePackageRepository;
    private final CodeGenerator codeGenerator;

    /**
     * Tạo thanh toán mới cho gói dịch vụ
     */
    @PostMapping
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<ApiResponseDto<PartnerPaymentResponse>> createPayment(
            @Valid @RequestBody PartnerPaymentCreateRequest request) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Partners partner = partnerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "username", username));

        PartnerPost post = partnerPostRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("PartnerPost", "id", request.getPostId()));

        // Verify ownership
        if (!post.getPartner().getId().equals(partner.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error(403, "Bạn không có quyền thanh toán cho tin đăng này", null));
        }

        // Create payment
        PartnerPayment payment = PartnerPayment.builder()
                .partner(partner)
                .post(post)
                .amount(request.getAmount())
                .method(request.getMethod())
                .build();

        PartnerPayment savedPayment = partnerPaymentRepository.save(payment);

        // Generate payment code after save
        String paymentCode = codeGenerator.generateCode("PAY", savedPayment.getId());
        savedPayment.setPaymentCode(paymentCode);
        PartnerPayment finalPayment = partnerPaymentRepository.save(savedPayment);

        PartnerPaymentResponse response = mapToResponse(finalPayment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(201, "Thanh toán thành công", response));
    }

    /**
     * Mô phỏng mua gói dịch vụ để test hệ thống (đơn giản hóa, chưa tích hợp cổng thanh toán)
     * - Tạo bản ghi thanh toán với amount = price của gói
     * - Cập nhật trạng thái bài đăng về ACTIVE và set approvedAt = now
     * - Giữ chỗ để tích hợp MoMo sau này
     */
    @PostMapping("/simulate-purchase")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<ApiResponseDto<PartnerPaymentResponse>> simulatePurchase(@RequestParam Long postId,
                                                                                   @RequestParam Long packageId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Partners partner = partnerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "username", username));

        PartnerPost post = partnerPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("PartnerPost", "id", postId));

        if (!post.getPartner().getId().equals(partner.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error(403, "Bạn không có quyền mua gói cho tin này", null));
        }

        ServicePackage pkg = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("ServicePackage", "id", packageId));

        // TODO: Tích hợp MoMo tại đây (khởi tạo giao dịch, callback, xác nhận)

        // Tạo thanh toán đơn giản (method = TEST)
        PartnerPayment payment = PartnerPayment.builder()
                .partner(partner)
                .post(post)
                .amount(pkg.getPrice())
                .method(PaymentMethod.CASH)
                .build();
        PartnerPayment savedPayment = partnerPaymentRepository.save(payment);
        String paymentCode = codeGenerator.generateCode("PAY", savedPayment.getId());
        savedPayment.setPaymentCode(paymentCode);
        PartnerPayment finalPayment = partnerPaymentRepository.save(savedPayment);

        // Kích hoạt bài đăng
        post.setStatus(PostApprovalStatus.APPROVED);
        post.setApprovedAt(java.time.LocalDateTime.now());
        // Giữ nguyên approvedBy (có thể là null); thực tế sẽ set bởi bộ phận duyệt
        partnerPostRepository.save(post);

        return ResponseEntity.ok(ApiResponseDto.success(200, "Mua gói thành công (mô phỏng)", mapToResponse(finalPayment)));
    }

    /**
     * Lấy lịch sử thanh toán của partner hiện tại
     */
    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<ApiResponseDto<List<PartnerPaymentResponse>>> getMyPayments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Partners partner = partnerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Partner", "username", username));

        List<PartnerPayment> payments = partnerPaymentRepository.findByPartnerId(partner.getId());
        List<PartnerPaymentResponse> responses = payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success(200, "Lấy lịch sử thanh toán thành công", responses));
    }

    // Helper method
    private PartnerPaymentResponse mapToResponse(PartnerPayment payment) {
        return PartnerPaymentResponse.builder()
                .id(payment.getId())
                .paymentCode(payment.getPaymentCode())
                .partnerId(payment.getPartner().getId())
                .partnerName(payment.getPartner().getCompanyName())
                .postId(payment.getPost().getId())
                .postTitle(payment.getPost().getTitle())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .paidDate(payment.getPaidDate())
                .build();
    }
}
