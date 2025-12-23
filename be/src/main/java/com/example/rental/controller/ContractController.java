package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.contract.ContractCreateRequest;
import com.example.rental.dto.contract.ContractUpdateRequest;
import com.example.rental.dto.contract.DepositPaymentRequest;
import com.example.rental.dto.contract.DepositMomoInitiateRequest;
import com.example.rental.dto.contract.DepositMomoInitiateResponse;
import com.example.rental.dto.contract.ContractResponse;
import com.example.rental.mapper.ContractMapper;
import com.example.rental.service.ContractService;
import com.example.rental.service.MomoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Contract Management", description = "Quản lý hợp đồng thuê phòng")
public class ContractController {

    private final ContractService contractService;
    private final ContractMapper contractMapper;
    private final MomoService momoService;
    private final com.example.rental.service.TenantService tenantService;
    private final com.example.rental.service.CheckoutService checkoutService;
    private final com.example.rental.service.InvoiceService invoiceService;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Operation(summary = "Tạo hợp đồng mới")
    @PostMapping
    public ResponseEntity<ApiResponseDto<ContractResponse>> createContract(
            @RequestBody ContractCreateRequest request) throws IOException {

        var contract = contractService.createContract(request);
        var response = contractMapper.toResponse(contract);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(), "Tạo hợp đồng thành công", response));
    }

    @Operation(summary = "Danh sách hợp đồng theo chi nhánh của tôi (ADMIN xem toàn bộ)")
    @GetMapping("/my-branch")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RECEPTIONIST','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<Page<ContractResponse>>> getMyBranchContracts(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "q", required = false) String q,
            Pageable pageable) {
        Page<com.example.rental.entity.Contract> page = contractService.getMyBranchContracts(status, q, pageable);
        Page<ContractResponse> responses = page.map(contractMapper::toResponse);
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Danh sách hợp đồng",
                responses
        ));
    }

    @Operation(summary = "Chi tiết hợp đồng (nhân viên)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RECEPTIONIST','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<ContractResponse>> getContractForStaff(@PathVariable Long id) {
        var contract = contractService.getContractForStaff(id);
        var response = contractMapper.toResponse(contract);
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Chi tiết hợp đồng",
                response
        ));
    }

    @Operation(summary = "Cập nhật hợp đồng (PENDING) - nhân viên")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RECEPTIONIST','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<ContractResponse>> updateContractForStaff(
            @PathVariable Long id,
            @RequestBody ContractUpdateRequest request) throws IOException {
        var contract = contractService.updateContractForStaff(id, request);
        var response = contractMapper.toResponse(contract);
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Cập nhật hợp đồng thành công",
                response
        ));
    }

    @Operation(summary = "Xóa hợp đồng tạm (PENDING) - nhân viên")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RECEPTIONIST','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<Void>> deletePendingContractForStaff(@PathVariable Long id) {
        contractService.deletePendingContractForStaff(id);
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Đã xóa hợp đồng tạm.",
                null
        ));
    }

    @Operation(summary = "Upload hợp đồng đã ký (ảnh/pdf)")
    @PostMapping(value = "/{id}/upload-signed", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RECEPTIONIST','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<ContractResponse>> uploadSignedContract(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) throws IOException {

        var contract = contractService.uploadSignedContract(id, file);
        var response = contractMapper.toResponse(contract);

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Upload hợp đồng đã ký thành công", response));
    }

    @Operation(summary = "Xác nhận thanh toán tiền cọc và kích hoạt hợp đồng")
    @PostMapping("/{id}/deposit/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RECEPTIONIST','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<ContractResponse>> confirmDepositPayment(
            @PathVariable Long id,
            @RequestBody DepositPaymentRequest request) throws IOException {
        var contract = contractService.confirmDepositPaymentForStaff(id, request);
        var response = contractMapper.toResponse(contract);
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "Xác nhận thanh toán tiền cọc thành công",
                response
        ));
    }

    @Operation(summary = "Khởi tạo thanh toán MoMo tiền cọc (trả về payUrl)")
    @PostMapping("/{id}/deposit/momo/initiate")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RECEPTIONIST','ACCOUNTANT')")
    public ResponseEntity<ApiResponseDto<DepositMomoInitiateResponse>> initiateDepositMomo(
            @PathVariable Long id,
            @RequestBody(required = false) DepositMomoInitiateRequest request) {

        var contract = contractService.getContractForStaff(id);
        if (contract.getStatus() == null || contract.getStatus() != com.example.rental.entity.ContractStatus.SIGNED_PENDING_DEPOSIT) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(400, "Hợp đồng chưa ở trạng thái chờ thanh toán tiền cọc.", "INVALID_STATUS", null));
        }

        java.math.BigDecimal amount = request != null ? request.getAmount() : null;
        if (amount == null) amount = contract.getDeposit();
        if (amount == null) amount = java.math.BigDecimal.ZERO;

        // Build return URL (best-effort)
        String returnPath = request != null ? request.getReturnPath() : null;
        if (returnPath == null || returnPath.isBlank() || !returnPath.startsWith("/")) {
            returnPath = "/staff/contracts";
        }
        String redirectUrl = (frontendBaseUrl == null || frontendBaseUrl.isBlank() ? "http://localhost:3000" : frontendBaseUrl) + returnPath;

        String orderId = "DEP-" + id + "-" + UUID.randomUUID();
        String orderInfo = "Thanh toan tien coc hop dong #" + id;
        String extraDataPlain = "CONTRACT_DEPOSIT:" + id;
        String extraData = Base64.getEncoder().encodeToString(extraDataPlain.getBytes(StandardCharsets.UTF_8));

        long momoAmount = amount.longValue();
        var momoResp = momoService.createATMPayment(momoAmount, orderId, orderInfo, redirectUrl, extraData);
        if (momoResp == null || momoResp.getPayUrl() == null || momoResp.getPayUrl().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(ApiResponseDto.error(502, "Không thể khởi tạo thanh toán MoMo", "MOMO_INIT_FAILED", null));
        }

        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(),
                "MoMo initiated",
                new DepositMomoInitiateResponse(momoResp.getPayUrl(), orderId)
        ));
    }

    @Operation(summary = "Lấy danh sách hợp đồng của người dùng hiện tại (Tenant)")
    @GetMapping("/my-contracts")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponseDto<java.util.List<ContractResponse>>> getMyContracts() {
        // Lấy username từ SecurityContext
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        var tenantOpt = tenantService.findByUsername(username);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.success(HttpStatus.NOT_FOUND.value(), "Không tìm thấy người thuê", null));
        }
        var tenant = tenantOpt.get();
        java.util.List<com.example.rental.entity.Contract> contracts = contractService.findByTenantId(tenant.getId());
        java.util.List<ContractResponse> responses = contracts.stream().map(contractMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "Danh sách hợp đồng của bạn", responses));
    }

    @Operation(summary = "Lấy danh sách hợp đồng của người dùng hiện tại (Tenant) - phân trang")
    @GetMapping("/my-contracts/paged")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponseDto<Page<ContractResponse>>> getMyContractsPaged(Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var tenantOpt = tenantService.findByUsername(username);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.success(HttpStatus.NOT_FOUND.value(), "Không tìm thấy người thuê", null));
        }
        var tenant = tenantOpt.get();
        Page<com.example.rental.entity.Contract> page = contractService.findByTenantId(tenant.getId(), pageable);
        Page<ContractResponse> responses = page.map(contractMapper::toResponse);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "Danh sách hợp đồng của bạn (phân trang)", responses));
    }

    @Operation(summary = "Người thuê gửi yêu cầu trả phòng (checkout request)")
    @PostMapping("/{id}/checkout-request")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('TENANT','GUEST')")
    public ResponseEntity<ApiResponseDto<com.example.rental.dto.checkout.CheckoutRequestResponse>> submitCheckoutRequest(
            @PathVariable Long id,
            @RequestBody com.example.rental.dto.checkout.CheckoutRequestDto request) {

        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        var resp = checkoutService.submitCheckoutRequest(id, username, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(), "Yêu cầu trả phòng đã gửi", resp));
    }

    @Operation(summary = "Nhân viên duyệt yêu cầu trả phòng (approve)")
    @PutMapping("/checkout-requests/{requestId}/approve")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDto<com.example.rental.dto.checkout.CheckoutRequestResponse>> approveCheckoutRequest(
            @PathVariable Long requestId) {
        String approver = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        var resp = checkoutService.approveRequest(requestId, approver);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "Yêu cầu đã được duyệt", resp));
    }

    @Operation(summary = "Finalize checkout: đóng hợp đồng và tạo hóa đơn cuối")
    @PostMapping("/{id}/finalize-checkout")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','MANAGER','RECEPTIONIST')")
    public ResponseEntity<ApiResponseDto<Void>> finalizeCheckout(@PathVariable Long id) {
        String operator = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        checkoutService.finalizeCheckout(id, operator);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "Hoàn tất trả phòng thành công"));
    }

    @Operation(summary = "Tải hợp đồng DOCX gốc")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadContract(@PathVariable Long id) throws IOException {
        Resource resource = contractService.downloadContract(id);
        Path filePath = Path.of(resource.getFile().getAbsolutePath());
        String contentType = Files.probeContentType(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }
}
