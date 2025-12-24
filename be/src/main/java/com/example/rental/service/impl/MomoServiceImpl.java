package com.example.rental.service.impl;

import com.example.rental.exception.SignatureVerificationException;
import com.example.rental.service.MomoClientService;
import com.example.rental.service.MomoService;
import com.example.rental.entity.PostApprovalStatus;
import com.example.rental.entity.PartnerPost;
import com.example.rental.dto.momo.CreateMomoResponse;
import com.example.rental.dto.momo.CreateMomoRequest;
import com.example.rental.dto.momo.QueryMomoRequest;
import com.example.rental.dto.momo.QueryMomoResponse;
import com.example.rental.repository.PartnerPostRepository;
import com.example.rental.repository.PartnerPaymentRepository;
import com.example.rental.repository.ContractRepository;
import com.example.rental.repository.ContractServiceRepository;
import com.example.rental.repository.RentalServiceRepository;
import com.example.rental.repository.RoomRepository;
import com.example.rental.repository.InvoiceRepository;
import com.example.rental.repository.PaymentRepository;
import com.example.rental.entity.PartnerPayment;
import com.example.rental.entity.PaymentMethod;
import com.example.rental.entity.ContractStatus;
import com.example.rental.entity.Contract;
import com.example.rental.entity.AuditAction;
import com.example.rental.service.AuditLogService;
import com.example.rental.utils.DepositDocxGenerator;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.Objects;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoServiceImpl implements MomoService {

    @Value(value = "${momo.partner-code}")
    private String PARTNER_CODE;
    @Value(value = "${momo.access-key}")
    private String ACCESS_KEY;
    @Value(value = "${momo.secret-key}")
    private String SECRET_KEY;
    @Value(value = "${momo.redirect-url}")
    private String REDIRECT_URL;
    @Value(value = "${momo.ipn-url}")
    private String IPN_URL;
    @Value(value = "${momo.request-type}")
    private String REQUEST_TYPE;

    private final MomoClientService momoClientService;
    private final PartnerPostRepository partnerPostRepository;
    private final PartnerPaymentRepository partnerPaymentRepository;
    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
        private final ContractServiceRepository contractServiceRepository;
        private final RentalServiceRepository rentalServiceRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final DepositDocxGenerator depositDocxGenerator;
    private final AuditLogService auditLogService;

        private void ensureUtilityServicesForContract(Contract contract) {
        if (contract == null || contract.getId() == null) return;
        java.util.List<com.example.rental.entity.ContractService> existing = contractServiceRepository.findByContractId(contract.getId());
        boolean hasElectricity = existing.stream().anyMatch(cs -> cs != null && cs.getService() != null
            && cs.getService().getServiceName() != null
            && cs.getService().getServiceName().equalsIgnoreCase("Điện"));
        boolean hasWater = existing.stream().anyMatch(cs -> cs != null && cs.getService() != null
            && cs.getService().getServiceName() != null
            && cs.getService().getServiceName().equalsIgnoreCase("Nước"));
        boolean hasSecurity = existing.stream().anyMatch(cs -> cs != null && cs.getService() != null
            && cs.getService().getServiceName() != null
            && cs.getService().getServiceName().equalsIgnoreCase("Bảo vệ 24/7"));

        if (!hasElectricity) {
            var electricity = rentalServiceRepository.findByServiceNameIgnoreCase("Điện")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình dịch vụ 'Điện'"));
            com.example.rental.entity.ContractService cs = com.example.rental.entity.ContractService.builder()
                .contract(contract)
                .service(electricity)
                .quantity(1)
                .startDate(contract.getStartDate() != null ? contract.getStartDate() : java.time.LocalDate.now())
                .endDate(null)
                .previousReading(null)
                .currentReading(null)
                .build();
            contractServiceRepository.save(cs);
        }

        if (!hasWater) {
            var water = rentalServiceRepository.findByServiceNameIgnoreCase("Nước")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình dịch vụ 'Nước'"));
            com.example.rental.entity.ContractService cs = com.example.rental.entity.ContractService.builder()
                .contract(contract)
                .service(water)
                .quantity(1)
                .startDate(contract.getStartDate() != null ? contract.getStartDate() : java.time.LocalDate.now())
                .endDate(null)
                .previousReading(null)
                .currentReading(null)
                .build();
            contractServiceRepository.save(cs);
        }

        if (!hasSecurity) {
            var security = rentalServiceRepository.findByServiceNameIgnoreCase("Bảo vệ 24/7")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình dịch vụ 'Bảo vệ 24/7'"));
            com.example.rental.entity.ContractService cs = com.example.rental.entity.ContractService.builder()
                .contract(contract)
                .service(security)
                .quantity(1)
                .startDate(contract.getStartDate() != null ? contract.getStartDate() : java.time.LocalDate.now())
                .endDate(null)
                .previousReading(null)
                .currentReading(null)
                .build();
            contractServiceRepository.save(cs);
        }
        }

    private static final String EXTRA_CONTRACT_DEPOSIT_PREFIX = "CONTRACT_DEPOSIT:";
    private static final String ORDER_CONTRACT_DEPOSIT_PREFIX = "DEP-";
    private static final String EXTRA_INVOICE_PREFIX = "INVOICE:";
    private static final String ORDER_INVOICE_PREFIX = "INV-";
    private static final String ORDER_PARTNER_POST_PREFIX = "POST-";

    @Override
    public CreateMomoResponse createATMPayment(long amount, String orderId) {

        // Locked to prevent using redirectUrl configs that contain hash fragments ("#")
        // which are not sent to the server and can break return verification.
        throw new UnsupportedOperationException(
                "Partner-post MoMo default initiation is locked. Use createATMPayment(amount, orderId, orderInfo, redirectUrl, extraData)."
        );
    }

    @Override
    public CreateMomoResponse createATMPayment(long amount, String orderId, String orderInfo, String redirectUrl, String extraData) {
        String requestId = UUID.randomUUID().toString();
        String safeOrderInfo = orderInfo == null ? "" : orderInfo;
        String safeRedirectUrl = (redirectUrl == null || redirectUrl.isBlank()) ? REDIRECT_URL : redirectUrl;
        String safeExtraData = extraData == null ? "" : extraData;

        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                ACCESS_KEY, amount, safeExtraData, IPN_URL, orderId, safeOrderInfo, PARTNER_CODE, safeRedirectUrl, requestId,
                REQUEST_TYPE);

        String prettySignature = "";
        log.debug("rawSignature" + rawSignature);
        try {
            prettySignature = signHmacSHA256(rawSignature, SECRET_KEY);
        } catch (Exception e) {
            log.error(">>>>Co loi khi hash code: ", e);
            return null;
        }

        if (prettySignature.isBlank()) {
            log.error(">>>> signature is blank");
            return null;
        }

        CreateMomoRequest request = CreateMomoRequest.builder()
                .partnerCode(PARTNER_CODE)
                .requestType(REQUEST_TYPE)
                .ipnUrl(IPN_URL)
                .redirectUrl(safeRedirectUrl)
                .orderId(orderId)
                .orderInfo(safeOrderInfo)
                .requestId(requestId)
                .extraData(safeExtraData)
                .amount(amount)
                .signature(prettySignature)
                .lang("vi")
                .build();

        return momoClientService.createATMPayment(request);
    }

    @Override
    public QueryMomoResponse queryTransaction(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Missing orderId");
        }

        // Many MoMo integrations use requestId = orderId for query.
        String requestId = orderId;

        String rawSignature = String.format(
                "accessKey=%s&orderId=%s&partnerCode=%s&requestId=%s",
                ACCESS_KEY,
                orderId,
                PARTNER_CODE,
                requestId
        );

        final String signature;
        try {
            signature = signHmacSHA256(rawSignature, SECRET_KEY);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign query request", e);
        }

        QueryMomoRequest req = QueryMomoRequest.builder()
                .partnerCode(PARTNER_CODE)
                .orderId(orderId)
                .requestId(requestId)
                .lang("vi")
                .signature(signature)
                .build();

        return momoClientService.queryTransaction(req);
    }

    @Override
    public boolean handleMomoReturn(String orderId) {
        if (orderId == null || orderId.isBlank()) return false;

        if (orderId.startsWith(ORDER_INVOICE_PREFIX)) {
            return handleMomoReturnForInvoice(orderId);
        }

        if (orderId.startsWith(ORDER_CONTRACT_DEPOSIT_PREFIX)) {
            return handleMomoReturnForContractDeposit(orderId);
        }

        if (orderId.startsWith(ORDER_PARTNER_POST_PREFIX)) {
            return handleMomoReturnForPartnerPost(orderId);
        }

        // Backward compatibility: partner posts previously used random UUID orderIds
        try {
            if (partnerPostRepository != null && partnerPostRepository.findByOrderId(orderId).isPresent()) {
                return handleMomoReturnForPartnerPost(orderId);
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    private boolean handleMomoReturnForPartnerPost(String orderId) {
        if (orderId == null || orderId.isBlank()) return false;

        PartnerPost post;
        try {
            post = partnerPostRepository.findByOrderId(orderId).orElse(null);
        } catch (Exception e) {
            post = null;
        }
        if (post == null) return false;

        // If already moved past payment stage, treat as success.
        try {
            if (post.getStatus() != null && post.getStatus() != PostApprovalStatus.PENDING_PAYMENT) {
                return true;
            }
        } catch (Exception ignored) {
        }

        QueryMomoResponse q = queryTransaction(orderId);
        if (q == null) return false;

        if (q.getResultCode() != 0) {
            try {
                post.setStatus(PostApprovalStatus.PENDING_PAYMENT);
                partnerPostRepository.save(post);
            } catch (Exception ignored) {
            }
            return false;
        }

        // Success: mark post ready for approval and record payment (idempotent by paymentCode)
        try {
            post.setStatus(PostApprovalStatus.PENDING_APPROVAL);
            partnerPostRepository.save(post);
        } catch (Exception ignored) {
        }

        try {
            String transId = q.getTransId();
            if (transId != null && !transId.isBlank() && !partnerPaymentRepository.existsByPaymentCode(transId)) {
                PartnerPayment payment = PartnerPayment.builder()
                        .paymentCode(transId)
                        .partner(post.getPartner())
                        .post(post)
                        .amount(java.math.BigDecimal.valueOf(q.getAmount()))
                        .method(PaymentMethod.MOMO)
                        .build();
                partnerPaymentRepository.save(payment);
            }
        } catch (Exception ignored) {
        }

        return true;
    }

    @Override
    public boolean handleMomoReturnForInvoice(String orderId) {
        if (orderId == null || orderId.isBlank()) return false;

        // Determine invoiceId from orderId: INV-<invoiceId>-...
        Long invoiceId = null;
        if (orderId.startsWith(ORDER_INVOICE_PREFIX)) {
            try {
                String[] parts = orderId.split("-");
                if (parts.length >= 2) {
                    invoiceId = Long.parseLong(parts[1]);
                }
            } catch (Exception ignored) {
            }
        }

        if (invoiceId == null) {
            return false;
        }

        final Long invId = invoiceId;

        try {
            // If already PAID, consider success.
            var invOpt = invoiceRepository.findById(invId);
            if (invOpt.isPresent() && invOpt.get().getStatus() == com.example.rental.entity.InvoiceStatus.PAID) {
                return true;
            }
        } catch (Exception ignored) {
        }

        QueryMomoResponse q = queryTransaction(orderId);
        if (q == null) return false;

        boolean ok = q.getResultCode() == 0;
        if (!ok) {
            return false;
        }

        markInvoicePaid(invId, q.getTransId(), orderId);

        // Best-effort: store payment reference (transId) and update Payment record.
        try {
            var invOpt = invoiceRepository.findById(invId);
            if (invOpt.isPresent()) {
                var inv = invOpt.get();
                if (inv.getPaymentReference() == null || inv.getPaymentReference().isBlank()) {
                    inv.setPaymentReference(q.getTransId());
                    invoiceRepository.save(inv);
                }
            }
        } catch (Exception ignored) {
        }

        try {
            paymentRepository.findAll().stream()
                    .filter(p -> (p.getProviderRef() != null && p.getProviderRef().equals(orderId))
                        || (p.getInvoiceId() != null && p.getInvoiceId().equals(invId)))
                    .findFirst()
                    .ifPresent(p -> {
                        p.setStatus("SUCCESS");
                        paymentRepository.save(p);
                    });
        } catch (Exception ignored) {
        }

        return true;
    }

    @Override
    public boolean handleMomoReturnForContractDeposit(String orderId) {
        if (orderId == null || orderId.isBlank()) return false;

        // Determine contractId from orderId: DEP-<contractId>-...
        Long contractId = null;
        if (orderId.startsWith(ORDER_CONTRACT_DEPOSIT_PREFIX)) {
            try {
                String[] parts = orderId.split("-");
                if (parts.length >= 2) {
                    contractId = Long.parseLong(parts[1]);
                }
            } catch (Exception ignored) {
            }
        }

        if (contractId == null) return false;

        final Long cId = contractId;

        try {
            var cOpt = contractRepository.findById(cId);
            if (cOpt.isPresent()) {
                var c = cOpt.get();
                // If already ACTIVE (deposit paid), consider success.
                if (c.getStatus() == ContractStatus.ACTIVE) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }

        QueryMomoResponse q = queryTransaction(orderId);
        if (q == null) return false;
        if (q.getResultCode() != 0) return false;

        // Reuse existing deposit-success logic (docs + activate contract)
        handleContractDepositSuccess(cId, q.getAmount(), q.getTransId(), orderId);
        return true;
    }

    @Override
    public void handleMomoCallback(Map<String, Object> payload) {
        log.info("Call back tu momo nhan duoc: {}", payload);

        String momoSignature = (String) payload.get("signature");
        if (momoSignature == null) {
            log.warn("Khong tim thay chu ky (signature) trong callback.");
            // Ném lỗi để Controller bắt
            throw new IllegalArgumentException("Invalid callback: Missing signature");
        }

        String amountStr = Objects.toString(payload.get("amount"), "");
        String extraData = Objects.toString(payload.get("extraData"), "");
        String message = Objects.toString(payload.get("message"), "");
        String orderId = Objects.toString(payload.get("orderId"), "");
        String orderInfo = Objects.toString(payload.get("orderInfo"), "");
        String orderType = Objects.toString(payload.get("orderType"), "");
        String partnerCode = Objects.toString(payload.get("partnerCode"), "");
        String payType = Objects.toString(payload.get("payType"), "");
        String requestId = Objects.toString(payload.get("requestId"), "");
        String responseTime = Objects.toString(payload.get("responseTime"), "");
        String resultCode = Objects.toString(payload.get("resultCode"), "");
        String transId = Objects.toString(payload.get("transId"), "");

        long amountLong = 0L;
        try {
            // Nếu amountStr là rỗng "" hoặc null, gán mặc định là 0
            if (amountStr == null || amountStr.isEmpty()) {
                amountLong = 0L;
            } else {
                amountLong = Long.parseLong(amountStr);
            }
        } catch (NumberFormatException e) {
            log.warn("Không thể ép kiểu 'amount' từ payload: {}", amountStr);
            // Ném lỗi nếu 'amount' là bắt buộc
            throw new IllegalArgumentException("Invalid amount format in callback");
        }

        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                ACCESS_KEY, amountLong, extraData, message, orderId, orderInfo, orderType, partnerCode, payType,
                requestId,
                responseTime, resultCode, transId);

        log.debug("rawSignature" + rawSignature);
        log.debug(SECRET_KEY);
        try {
            String mySignature = signHmacSHA256(rawSignature, SECRET_KEY);
            log.debug("Chu ky MoMo: {}", momoSignature);
            log.debug("Chu ky cua toi: {}", mySignature);

            if (!mySignature.equals(momoSignature)) {
                log.warn("XAC THUC CHU KY THAT BAI, Callback co the da bi gia mao.");
                // Ném lỗi bảo mật
                throw new SignatureVerificationException("Invalid signature");
            }

            log.info("Xac thuc chu ky thanh cong cho orderId: {}", orderId);

            // --- Contract deposit callback routing ---
            Long contractId = null;

            // --- Invoice callback routing ---
            Long invoiceId = null;

            // 1) Try decode extraData (MoMo convention is Base64)
            String extraDecoded = null;
            if (extraData != null && !extraData.isBlank()) {
                try {
                    byte[] decoded = Base64.getDecoder().decode(extraData);
                    extraDecoded = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception ignored) {
                    extraDecoded = null;
                }
            }

            if (extraDecoded != null && extraDecoded.startsWith(EXTRA_INVOICE_PREFIX)) {
                try {
                    invoiceId = Long.parseLong(extraDecoded.substring(EXTRA_INVOICE_PREFIX.length()).trim());
                } catch (Exception ignored) {
                }
            }

            if (extraDecoded != null && extraDecoded.startsWith(EXTRA_CONTRACT_DEPOSIT_PREFIX)) {
                try {
                    contractId = Long.parseLong(extraDecoded.substring(EXTRA_CONTRACT_DEPOSIT_PREFIX.length()).trim());
                } catch (Exception ignored) {
                }
            }

            // 2) Fallback: raw extraData (if it was not Base64)
            if (invoiceId == null && extraData != null && extraData.startsWith(EXTRA_INVOICE_PREFIX)) {
                try {
                    invoiceId = Long.parseLong(extraData.substring(EXTRA_INVOICE_PREFIX.length()).trim());
                } catch (Exception ignored) {
                }
            }
            if (contractId == null && extraData != null && extraData.startsWith(EXTRA_CONTRACT_DEPOSIT_PREFIX)) {
                try {
                    contractId = Long.parseLong(extraData.substring(EXTRA_CONTRACT_DEPOSIT_PREFIX.length()).trim());
                } catch (Exception ignored) {
                }
            }

            // 3) Fallback: parse from orderId format DEP-{contractId}-...
            if (invoiceId == null && orderId != null && orderId.startsWith(ORDER_INVOICE_PREFIX)) {
                try {
                    String[] parts = orderId.split("-");
                    if (parts.length >= 2) {
                        invoiceId = Long.parseLong(parts[1]);
                    }
                } catch (Exception ignored) {
                }
            }
            if (contractId == null && orderId != null && orderId.startsWith(ORDER_CONTRACT_DEPOSIT_PREFIX)) {
                try {
                    String[] parts = orderId.split("-");
                    if (parts.length >= 2) {
                        contractId = Long.parseLong(parts[1]);
                    }
                } catch (Exception ignored) {
                }
            }

            if (invoiceId != null) {
                final Long invId = invoiceId;
                final String ordId = orderId;
                if ("0".equals(resultCode)) {
                    handleInvoicePaymentSuccess(invId, amountLong, transId, ordId);
                } else {
                    log.info("Invoice payment FAILED for invoiceId={}, resultCode={}", invId, resultCode);
                    try {
                        paymentRepository.findAll().stream()
                                .filter(p -> (p.getProviderRef() != null && p.getProviderRef().equals(ordId))
                                        || (p.getInvoiceId() != null && p.getInvoiceId().equals(invId)))
                                .findFirst()
                                .ifPresent(p -> {
                                    p.setStatus("FAILED");
                                    paymentRepository.save(p);
                                });
                    } catch (Exception ignored) {
                    }
                    try {
                        auditLogService.logAction(
                                "SYSTEM",
                                "SYSTEM",
                                AuditAction.CONFIRM_PAYMENT,
                                "INVOICE",
                                invId,
                                "MoMo IPN - invoice payment failed",
                                null,
                                "{\"orderId\":\"" + ordId + "\",\"transId\":\"" + transId + "\",\"resultCode\":\"" + resultCode + "\"}",
                                "momo-ipn",
                                null,
                                "momo-ipn",
                                "FAILURE",
                                null
                        );
                    } catch (Exception ignored) {
                    }
                }
                return;
            }

            if (contractId != null) {
                if ("0".equals(resultCode)) {
                    handleContractDepositSuccess(contractId, amountLong, transId, orderId);
                } else {
                    log.info("Contract deposit payment FAILED for contractId={}, resultCode={}", contractId, resultCode);
                    try {
                        auditLogService.logAction(
                                "SYSTEM",
                                "SYSTEM",
                                AuditAction.CONFIRM_PAYMENT,
                                "CONTRACT",
                                contractId,
                                "MoMo IPN - deposit failed",
                                null,
                                "{\"orderId\":\"" + orderId + "\",\"transId\":\"" + transId + "\",\"resultCode\":\"" + resultCode + "\"}",
                                "momo-ipn",
                                null,
                                "momo-ipn",
                                "FAILURE",
                                null
                        );
                    } catch (Exception ignored) {
                    }
                }
                return;
            }

            PartnerPost post = partnerPostRepository.findByOrderId(orderId)
                    .orElseThrow(
                            () -> new EntityNotFoundException("Khong tim thay bai dang moi voi orderId: " + orderId));

            if ("0".equals(resultCode)) { // 0 = Thành công
                log.info("Thanh toan THANH CONG cho orderId: {}. Cap nhat trang thai...", orderId);
                post.setStatus(PostApprovalStatus.PENDING_APPROVAL);
                partnerPostRepository.save(post);

                // Tạo bản ghi PartnerPayment
                if (transId != null && !transId.isBlank() && !partnerPaymentRepository.existsByPaymentCode(transId)) {
                    PartnerPayment payment = PartnerPayment.builder()
                            .paymentCode(transId) // Dùng transId từ MoMo làm mã thanh toán
                            .partner(post.getPartner())
                            .post(post)
                            .amount(BigDecimal.valueOf(amountLong))
                            .method(PaymentMethod.MOMO)
                            .build();
                    partnerPaymentRepository.save(payment);
                    log.info("Đã lưu thông tin thanh toán cho orderId: {}, transId: {}", orderId, transId);
                } else {
                    log.info("Bo qua luu PartnerPayment do trung paymentCode/transId: {}", transId);
                }
            } else {
                log.info("Thanh toan THAT BAI cho orderId: {}. Ma loi: {}. Cap nhat trang thai...", orderId,
                        resultCode);
                post.setStatus(PostApprovalStatus.PENDING_PAYMENT);
                partnerPostRepository.save(post);
            }

        } catch (SignatureVerificationException e) {
            // Ném lại lỗi bảo mật để Controller xử lý
            throw e;
        } catch (Exception e) {
            // Ném lỗi chung nếu có vấn đề khi băm
            log.error("Loi khi xu ly callback: ", e);
            throw new RuntimeException("Callback processing error", e);
        }
    }

    private void handleContractDepositSuccess(Long contractId, long amountLong, String transId, String orderId) {
        var contractOpt = contractRepository.findById(contractId);
        if (contractOpt.isEmpty()) {
            log.warn("Contract not found for deposit callback: contractId={}", contractId);
            return;
        }

        var contract = contractOpt.get();
        if (contract.getStatus() == null || contract.getStatus() != ContractStatus.SIGNED_PENDING_DEPOSIT) {
            log.info("Ignoring deposit callback: contract not in SIGNED_PENDING_DEPOSIT. contractId={}, status={}", contractId, contract.getStatus());
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(amountLong);

        try {
            contract.setDepositPaymentMethod(PaymentMethod.MOMO);
            contract.setDepositPaidDate(LocalDateTime.now());
            contract.setDepositPaymentReference(transId);

            // Generate docs for accounting/printing
            DepositDocxGenerator.Result docs = depositDocxGenerator.generate(contract, amount, PaymentMethod.MOMO, transId, null);
            String invoiceUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath().path(docs.getInvoicePath()).toUriString();
            String receiptUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath().path(docs.getReceiptPath()).toUriString();
            contract.setDepositInvoiceUrl(invoiceUrl);
            contract.setDepositReceiptUrl(receiptUrl);

            contract.setStatus(ContractStatus.ACTIVE);

            // Auto attach electricity/water services (utilities)
            ensureUtilityServicesForContract(contract);

            if (contract.getRoom() != null) {
                contract.getRoom().setStatus(com.example.rental.entity.RoomStatus.OCCUPIED);
                roomRepository.save(contract.getRoom());
            }

            contractRepository.save(contract);

            try {
                auditLogService.logAction(
                        "SYSTEM",
                        "SYSTEM",
                        AuditAction.CONFIRM_PAYMENT,
                        "CONTRACT",
                        contractId,
                        "MoMo IPN - deposit confirmed",
                        null,
                        "{\"orderId\":\"" + orderId + "\",\"transId\":\"" + transId + "\",\"amount\":" + amountLong + "}",
                        "momo-ipn",
                        null,
                        "momo-ipn",
                        "SUCCESS",
                        null
                );
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            log.error("Failed to activate contract after MoMo deposit payment. contractId={}", contractId, e);
        }
    }

    private void handleInvoicePaymentSuccess(Long invoiceId, long amountLong, String transId, String orderId) {
        markInvoicePaid(invoiceId, transId, orderId);

        // Best-effort update payment record
        try {
            paymentRepository.findAll().stream()
                    .filter(p -> (p.getProviderRef() != null && p.getProviderRef().equals(orderId))
                            || (p.getInvoiceId() != null && p.getInvoiceId().equals(invoiceId)))
                    .findFirst()
                    .ifPresent(p -> {
                        p.setStatus("SUCCESS");
                        paymentRepository.save(p);
                    });
        } catch (Exception ignored) {
        }

        try {
            auditLogService.logAction(
                    "SYSTEM",
                    "SYSTEM",
                    AuditAction.CONFIRM_PAYMENT,
                    "INVOICE",
                    invoiceId,
                    "MoMo IPN - invoice payment confirmed",
                    null,
                    "{\"orderId\":\"" + orderId + "\",\"transId\":\"" + transId + "\",\"amount\":" + amountLong + "}",
                    "momo-ipn",
                    null,
                    "momo-ipn",
                    "SUCCESS",
                    null
            );
        } catch (Exception ignored) {
        }
    }

    private void markInvoicePaid(Long invoiceId, String transId, String orderId) {
        if (invoiceId == null) return;

        try {
            var invOpt = invoiceRepository.findById(invoiceId);
            if (invOpt.isEmpty()) {
                log.warn("Invoice not found for MoMo confirmation: invoiceId={}", invoiceId);
                return;
            }

            var inv = invOpt.get();
            if (inv.getStatus() == com.example.rental.entity.InvoiceStatus.PAID) {
                return;
            }

            inv.setStatus(com.example.rental.entity.InvoiceStatus.PAID);
            inv.setPaidDate(java.time.LocalDate.now());
            inv.setPaidDirect(Boolean.FALSE);
            String ref = (transId != null && !transId.isBlank()) ? transId : orderId;
            inv.setPaymentReference(ref);
            invoiceRepository.save(inv);
        } catch (Exception e) {
            log.error("Failed to mark invoice paid. invoiceId={}, orderId={}", invoiceId, orderId, e);
        }

        // Best-effort update payment record
        try {
            paymentRepository.findAll().stream()
                    .filter(p -> (p.getProviderRef() != null && p.getProviderRef().equals(orderId))
                            || (p.getInvoiceId() != null && p.getInvoiceId().equals(invoiceId)))
                    .findFirst()
                    .ifPresent(p -> {
                        p.setStatus("SUCCESS");
                        paymentRepository.save(p);
                    });
        } catch (Exception ignored) {
        }
    }

    private String signHmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKey);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}