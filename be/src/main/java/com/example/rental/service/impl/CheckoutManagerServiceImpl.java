package com.example.rental.service.impl;

import com.example.rental.dto.checkout.CheckoutRequestManagerRow;
import com.example.rental.dto.damage.DamageReportCreateRequest;
import com.example.rental.dto.damage.DamageReportResponse;
import com.example.rental.dto.invoice.InvoiceResponse;
import com.example.rental.entity.*;
import com.example.rental.exception.BadRequestException;
import com.example.rental.mapper.InvoiceMapper;
import com.example.rental.repository.*;
import com.example.rental.service.CheckoutManagerService;
import com.example.rental.service.EmailService;
import com.example.rental.utils.InvoiceEmailTemplateUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CheckoutManagerServiceImpl implements CheckoutManagerService {

    private final CheckoutRequestRepository checkoutRequestRepository;
    private final DamageReportRepository damageReportRepository;
    private final DamageImageRepository damageImageRepository;
    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    private static final Path DAMAGE_UPLOAD_PATH = Paths.get(System.getProperty("user.dir"), "uploads", "damage");

    private Employees getCurrentEmployee() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return employeeRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Không tìm thấy nhân viên đang đăng nhập"));
    }

    private void requireManagerRole() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            throw new AccessDeniedException("Không có quyền");
        }
        boolean ok = auth.getAuthorities().stream().anyMatch(a -> "ROLE_MANAGER".equals(a.getAuthority()));
        if (!ok) {
            throw new AccessDeniedException("Chỉ quản lý mới được thực hiện thao tác này");
        }
    }

    private String getMyBranchCode() {
        Employees e = getCurrentEmployee();
        if (e.getBranch() == null || e.getBranch().getBranchCode() == null) {
            throw new AccessDeniedException("Không xác định được chi nhánh của nhân viên");
        }
        return e.getBranch().getBranchCode();
    }

    private CheckoutRequestManagerRow toRow(CheckoutRequest cr) {
        CheckoutRequestManagerRow row = new CheckoutRequestManagerRow();
        row.setId(cr.getId());
        row.setStatus(cr.getStatus() != null ? cr.getStatus().name() : null);
        row.setReason(cr.getReason());
        row.setCreatedAt(cr.getCreatedAt());

        Contract c = cr.getContract();
        if (c != null) {
            row.setContractId(c.getId());
            row.setRoomNumber(c.getRoomNumber());
            row.setBranchCode(c.getBranchCode());
            row.setRoomCode(c.getRoom() != null ? c.getRoom().getRoomCode() : null);
        }

        Tenant t = cr.getTenant();
        if (t != null) {
            row.setTenantId(t.getId());
            row.setTenantName(t.getFullName());
            row.setTenantPhoneNumber(t.getPhoneNumber());
        }

        return row;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CheckoutRequestManagerRow> listMyBranchRequests(List<String> statuses, Pageable pageable) {
        requireManagerRole();
        String branchCode = getMyBranchCode();

        List<CheckoutStatus> statusEnums = new ArrayList<>();
        if (statuses == null || statuses.isEmpty()) {
            statusEnums.add(CheckoutStatus.PENDING);
            statusEnums.add(CheckoutStatus.APPROVED);
        } else {
            for (String s : statuses) {
                if (s == null || s.isBlank()) continue;
                statusEnums.add(CheckoutStatus.valueOf(s.trim().toUpperCase()));
            }
        }

        return checkoutRequestRepository.findForBranchAndStatuses(branchCode, statusEnums, pageable)
                .map(this::toRow);
    }

    @Override
    @Transactional
    public CheckoutRequestManagerRow approve(Long requestId) {
        requireManagerRole();
        CheckoutRequest req = checkoutRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu trả phòng"));

        String myBranch = getMyBranchCode();
        String contractBranch = req.getContract() != null ? req.getContract().getBranchCode() : null;
        if (contractBranch == null || !contractBranch.equalsIgnoreCase(myBranch)) {
            throw new AccessDeniedException("Bạn không có quyền duyệt yêu cầu của chi nhánh khác");
        }

        if (req.getStatus() == CheckoutStatus.COMPLETED) {
            return toRow(req);
        }

        req.setStatus(CheckoutStatus.APPROVED);
        checkoutRequestRepository.save(req);
        return toRow(req);
    }

    private DamageReport getOrCreateReportEntity(Long requestId) {
        requireManagerRole();
        CheckoutRequest req = checkoutRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu trả phòng"));

        String myBranch = getMyBranchCode();
        String contractBranch = req.getContract() != null ? req.getContract().getBranchCode() : null;
        if (contractBranch == null || !contractBranch.equalsIgnoreCase(myBranch)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập yêu cầu của chi nhánh khác");
        }

        return damageReportRepository.findByCheckoutRequestId(requestId)
                .orElseGet(() -> {
                    Employees inspector = getCurrentEmployee();
                    Contract contract = req.getContract();
                    if (contract == null) {
                        throw new EntityNotFoundException("Yêu cầu không có hợp đồng");
                    }

                    DamageReport dr = DamageReport.builder()
                            .contract(contract)
                            .checkoutRequest(req)
                            .inspector(inspector)
                            .description("Biên bản kiểm tra trả phòng")
                            .damageDetails("{}")
                            .totalDamageCost(BigDecimal.ZERO)
                            .status(DamageReportStatus.DRAFT)
                            .build();

                    return damageReportRepository.save(dr);
                });
    }

    private DamageReportResponse toDamageReportResponse(DamageReport dr) {
        // Reuse existing mapping logic by creating minimal response here
        DamageReportResponse resp = new DamageReportResponse();
        resp.setId(dr.getId());
        resp.setCheckoutRequestId(dr.getCheckoutRequest() != null ? dr.getCheckoutRequest().getId() : null);
        resp.setContractId(dr.getContract() != null ? dr.getContract().getId() : null);
        resp.setContractCode(dr.getContract() != null ? "C-" + dr.getContract().getId() : null);
        resp.setTenantName(dr.getContract() != null && dr.getContract().getTenant() != null ? dr.getContract().getTenant().getFullName() : "N/A");
        resp.setRoomCode(dr.getContract() != null && dr.getContract().getRoom() != null ? dr.getContract().getRoom().getRoomCode() : null);
        resp.setInspectorName(dr.getInspector() != null ? dr.getInspector().getFullName() : "N/A");
        resp.setDescription(dr.getDescription());
        resp.setDamageDetails(dr.getDamageDetails());
        resp.setTotalDamageCost(dr.getTotalDamageCost());
        resp.setSettlementInvoiceId(dr.getSettlementInvoiceId());
        resp.setStatus(dr.getStatus() != null ? dr.getStatus().name() : null);
        resp.setApproverName(dr.getApprover() != null ? dr.getApprover().getFullName() : null);
        resp.setApproverNote(dr.getApproverNote());
        resp.setCreatedAt(dr.getCreatedAt());
        resp.setApprovedAt(dr.getApprovedAt());

        if (dr.getImages() != null) {
            List<com.example.rental.dto.damage.DamageImageDto> imgs = new ArrayList<>();
            for (DamageImage img : dr.getImages()) {
                com.example.rental.dto.damage.DamageImageDto dto = new com.example.rental.dto.damage.DamageImageDto();
                dto.setId(img.getId());
                dto.setImageUrl(img.getImageUrl());
                dto.setDescription(img.getDescription());
                imgs.add(dto);
            }
            resp.setImages(imgs);
        }

        return resp;
    }

    @Override
    @Transactional
    public DamageReportResponse getOrCreateInspectionReport(Long requestId) {
        DamageReport dr = getOrCreateReportEntity(requestId);
        return toDamageReportResponse(dr);
    }

    @Override
    @Transactional
    public DamageReportResponse saveInspectionReport(Long requestId, DamageReportCreateRequest request) {
        DamageReport dr = getOrCreateReportEntity(requestId);

        if (dr.getStatus() != DamageReportStatus.DRAFT) {
            throw new BadRequestException("Không thể chỉnh sửa biên bản ở trạng thái: " + dr.getStatus());
        }

        if (request != null) {
            if (request.getDescription() != null) {
                dr.setDescription(request.getDescription());
            }
            if (request.getDamageDetails() != null) {
                dr.setDamageDetails(request.getDamageDetails());
            }
            if (request.getTotalDamageCost() != null) {
                dr.setTotalDamageCost(request.getTotalDamageCost());
            }
        }

        dr.setInspector(getCurrentEmployee());
        DamageReport saved = damageReportRepository.save(dr);
        return toDamageReportResponse(saved);
    }

    @Override
    @Transactional
    public DamageReportResponse uploadItemImages(Long requestId, String itemKey, List<MultipartFile> images) throws IOException {
        DamageReport dr = getOrCreateReportEntity(requestId);

        if (images == null || images.isEmpty()) {
            return toDamageReportResponse(dr);
        }

        Files.createDirectories(DAMAGE_UPLOAD_PATH);

        List<DamageImage> savedImages = new ArrayList<>();
        for (MultipartFile file : images) {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destPath = DAMAGE_UPLOAD_PATH.resolve(filename);
            File dest = destPath.toFile();
            Files.createDirectories(destPath.getParent());
            file.transferTo(dest);

            String desc = "itemKey:" + (itemKey == null ? "" : itemKey) + ";" + (file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
            DamageImage di = DamageImage.builder()
                    .damageReport(dr)
                    .imageUrl("/uploads/damage/" + filename)
                    .description(desc)
                    .build();
            savedImages.add(di);
        }

        damageImageRepository.saveAll(savedImages);
        // refresh images list
        if (dr.getImages() == null) {
            dr.setImages(new ArrayList<>());
        }
        dr.getImages().addAll(savedImages);
        damageReportRepository.save(dr);

        return toDamageReportResponse(dr);
    }

    private List<InvoiceDetail> buildInvoiceDetailsFromReport(DamageReport dr) {
        List<InvoiceDetail> details = new ArrayList<>();

        if (dr.getDamageDetails() == null || dr.getDamageDetails().isBlank()) {
            return details;
        }

        try {
            JsonNode root = objectMapper.readTree(dr.getDamageDetails());
            JsonNode items = root;
            if (root != null && root.has("items")) {
                items = root.get("items");
            }

            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    if (item == null) continue;
                    String label = item.hasNonNull("label") ? item.get("label").asText() : null;
                    if (label == null || label.isBlank()) {
                        label = item.hasNonNull("key") ? item.get("key").asText() : "Chi phí";
                    }

                    BigDecimal amount = BigDecimal.ZERO;
                    if (item.hasNonNull("amount")) {
                        try {
                            amount = new BigDecimal(item.get("amount").asText());
                        } catch (Exception ignored) {
                            amount = BigDecimal.ZERO;
                        }
                    }

                    if (amount.signum() <= 0) continue;

                    InvoiceDetail d = InvoiceDetail.builder()
                            .invoice(null)
                            .description(label)
                            .unitPrice(amount)
                            .quantity(1)
                            .amount(amount)
                            .build();
                    details.add(d);
                }
            }
        } catch (Exception ignored) {
            // ignore malformed JSON
        }

        return details;
    }

    @Override
    @Transactional
    public InvoiceResponse createSettlementInvoice(Long requestId, LocalDate dueDate) {
        requireManagerRole();
        CheckoutRequest req = checkoutRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu trả phòng"));

        String myBranch = getMyBranchCode();
        String contractBranch = req.getContract() != null ? req.getContract().getBranchCode() : null;
        if (contractBranch == null || !contractBranch.equalsIgnoreCase(myBranch)) {
            throw new AccessDeniedException("Bạn không có quyền tạo hóa đơn cho chi nhánh khác");
        }

        DamageReport dr = getOrCreateReportEntity(requestId);

        if (dr.getSettlementInvoiceId() != null) {
            Invoice existing = invoiceRepository.findById(dr.getSettlementInvoiceId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn đã tạo"));
            return InvoiceMapper.toResponse(existing);
        }

        LocalDate safeDue = dueDate != null ? dueDate : LocalDate.now();

        List<InvoiceDetail> details = buildInvoiceDetailsFromReport(dr);
        if (details.isEmpty()) {
            throw new BadRequestException("Biên bản chưa có khoản thu nào (số tiền phải > 0)");
        }

        BigDecimal total = details.stream().map(InvoiceDetail::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Invoice inv = Invoice.builder()
                .contract(req.getContract())
                .dueDate(safeDue)
                .status(InvoiceStatus.UNPAID)
                .amount(total)
                .build();

        Invoice saved = invoiceRepository.save(inv);

        for (InvoiceDetail d : details) {
            d.setInvoice(saved);
        }

        saved.setDetails(details);
        invoiceDetailRepository.saveAll(details);
        invoiceRepository.save(saved);

        dr.setSettlementInvoiceId(saved.getId());
        damageReportRepository.save(dr);

        Tenant tenant = req.getContract().getTenant();
        if (tenant != null && tenant.getEmail() != null && !tenant.getEmail().isBlank()) {
            String subject = "[Rental] Hóa đơn trả phòng #" + saved.getId();
            String html = InvoiceEmailTemplateUtil.buildNewInvoiceEmail(saved, tenant);
            emailService.sendHtmlMessage(tenant.getEmail(), subject, html);
        }

        return InvoiceMapper.toResponse(saved);
    }
}
