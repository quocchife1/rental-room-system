package com.example.rental.service.impl;

import com.example.rental.exception.SignatureVerificationException;
import com.example.rental.service.MomoClientService;
import com.example.rental.service.MomoService;
import com.example.rental.service.PartnerPostService;
import com.example.rental.entity.PostApprovalStatus;
import com.example.rental.entity.PartnerPost;
import com.example.rental.dto.momo.CreateMomoResponse;
import com.example.rental.dto.momo.CreateMomoRequest;
import com.example.rental.repository.PartnerPostRepository;
import com.example.rental.repository.PartnerPaymentRepository;
import com.example.rental.repository.ContractRepository;
import com.example.rental.repository.ContractServiceRepository;
import com.example.rental.repository.RentalServiceRepository;
import com.example.rental.repository.RoomRepository;
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
import org.springframework.context.annotation.Lazy;
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

    @Override
    public CreateMomoResponse createATMPayment(long amount, String orderId) {

        return createATMPayment(amount, orderId, "Thanh toan bai dang", REDIRECT_URL, "");
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

            if (extraDecoded != null && extraDecoded.startsWith(EXTRA_CONTRACT_DEPOSIT_PREFIX)) {
                try {
                    contractId = Long.parseLong(extraDecoded.substring(EXTRA_CONTRACT_DEPOSIT_PREFIX.length()).trim());
                } catch (Exception ignored) {
                }
            }

            // 2) Fallback: raw extraData (if it was not Base64)
            if (contractId == null && extraData != null && extraData.startsWith(EXTRA_CONTRACT_DEPOSIT_PREFIX)) {
                try {
                    contractId = Long.parseLong(extraData.substring(EXTRA_CONTRACT_DEPOSIT_PREFIX.length()).trim());
                } catch (Exception ignored) {
                }
            }

            // 3) Fallback: parse from orderId format DEP-{contractId}-...
            if (contractId == null && orderId != null && orderId.startsWith(ORDER_CONTRACT_DEPOSIT_PREFIX)) {
                try {
                    String[] parts = orderId.split("-");
                    if (parts.length >= 2) {
                        contractId = Long.parseLong(parts[1]);
                    }
                } catch (Exception ignored) {
                }
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