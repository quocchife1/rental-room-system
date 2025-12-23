package com.example.rental.service.impl;

import com.example.rental.dto.invoice.*;
import com.example.rental.entity.*;
import com.example.rental.mapper.InvoiceMapper;
import com.example.rental.repository.*;
import com.example.rental.security.Audited;
import com.example.rental.service.EmailService;
import com.example.rental.service.InvoiceService;
import com.example.rental.utils.InvoiceEmailTemplateUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final ContractRepository contractRepository;
    private final ContractServiceRepository contractServiceRepository;
    private final ServiceBookingRepository serviceBookingRepository;
    private final EmailService emailService;
    private final DamageReportRepository damageReportRepository;
    private final CheckoutRequestRepository checkoutRequestRepository;
    private final EmployeeRepository employeeRepository;

    // Matches DECIMAL(12,2) columns (max: 9,999,999,999.99)
    private static final BigDecimal MAX_MONEY_DECIMAL_12_2 = new BigDecimal("9999999999.99");

    private static String safeTenantName(Tenant t) {
        if (t == null) return null;
        try {
            if (t.getFullName() != null && !t.getFullName().isBlank()) return t.getFullName();
        } catch (Exception ignored) {
        }
        try {
            return t.getUsername();
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<InvoiceDetailResponse> computeInvoiceDetailsPreview(Contract contract, int billingYear, int billingMonth, LocalDate dueDate) {
        if (contract == null) throw new IllegalArgumentException("Thiếu hợp đồng");
        LocalDate periodStart = LocalDate.of(billingYear, billingMonth, 1);
        LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);

        List<InvoiceDetailResponse> details = new ArrayList<>();

        // 1. Room rent
        if (contract.getRoom() != null && contract.getRoom().getPrice() != null) {
            BigDecimal unit = contract.getRoom().getPrice();
            details.add(InvoiceDetailResponse.builder()
                    .id(null)
                    .description("Tiền phòng tháng " + billingMonth + "/" + billingYear)
                    .unitPrice(unit)
                    .quantity(1)
                    .amount(unit)
                    .build());
        }

        // 2. Contract services in period (NO persistence side-effects)
        if (contract.getServices() != null) {
            for (ContractService cs : contract.getServices()) {
                if (cs == null || cs.getService() == null) continue;

                LocalDate csStart = cs.getStartDate();
                LocalDate csEnd = cs.getEndDate();
                boolean activeInPeriod = (csStart == null || !csStart.isAfter(periodEnd))
                        && (csEnd == null || !csEnd.isBefore(periodStart));
                if (!activeInPeriod) continue;

                RentalServiceItem service = cs.getService();
                BigDecimal unitPrice = service.getPrice() != null ? service.getPrice() : BigDecimal.ZERO;
                Integer quantity = cs.getQuantity() != null ? cs.getQuantity() : 1;

                boolean isElectricity = service.getServiceName() != null && service.getServiceName().equalsIgnoreCase("Điện");
                boolean isWater = service.getServiceName() != null && service.getServiceName().equalsIgnoreCase("Nước");

                if (isElectricity || isWater) {
                    if (cs.getPreviousReading() == null || cs.getCurrentReading() == null) {
                        throw new IllegalStateException("Thiếu chỉ số " + service.getServiceName() + " cho hợp đồng #" + contract.getId() + " trong kỳ " + billingMonth + "/" + billingYear);
                    }

                    BigDecimal usage = cs.getCurrentReading().subtract(cs.getPreviousReading());
                    if (usage.signum() < 0) {
                        throw new IllegalStateException("Chỉ số " + service.getServiceName() + " không hợp lệ (current < previous) cho hợp đồng #" + contract.getId());
                    }
                    quantity = usage.intValue();
                }

                BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
                details.add(InvoiceDetailResponse.builder()
                        .id(null)
                        .description(service.getServiceName() + " tháng " + billingMonth + "/" + billingYear)
                        .unitPrice(unitPrice)
                        .quantity(quantity)
                        .amount(amount)
                        .build());
            }
        }

        // 3. Scheduled services (bookings) in period
        try {
            var billableBookings = serviceBookingRepository.findByContract_IdAndStatusInAndBookingDateBetween(
                    contract.getId(),
                    java.util.List.of(
                            com.example.rental.entity.ServiceBookingStatus.BOOKED,
                            com.example.rental.entity.ServiceBookingStatus.COMPLETED
                    ),
                    periodStart,
                    periodEnd
            );

            if (billableBookings != null && !billableBookings.isEmpty()) {
                java.util.Map<Long, java.util.List<com.example.rental.entity.ServiceBooking>> byService = billableBookings.stream()
                        .filter(b -> b != null && b.getService() != null && b.getService().getId() != null)
                        .collect(java.util.stream.Collectors.groupingBy(b -> b.getService().getId()));

                for (var entry : byService.entrySet()) {
                    var bookings = entry.getValue();
                    if (bookings == null || bookings.isEmpty()) continue;
                    var service = bookings.get(0).getService();
                    int qty = bookings.size();
                    BigDecimal unitPrice = service.getPrice() != null ? service.getPrice() : BigDecimal.ZERO;
                    BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(qty));

                    details.add(InvoiceDetailResponse.builder()
                            .id(null)
                            .description(service.getServiceName() + " (" + qty + " lần) tháng " + billingMonth + "/" + billingYear)
                            .unitPrice(unitPrice)
                            .quantity(qty)
                            .amount(amount)
                            .build());
                }
            }
        } catch (Exception ignored) {
            // best-effort
        }

        return details;
    }

    private void assertTenantOwnsInvoiceIfTenant(Invoice invoice) {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getAuthorities() == null) return;
            boolean isTenant = auth.getAuthorities().stream().anyMatch(a -> "ROLE_TENANT".equals(a.getAuthority()));
            if (!isTenant) return;

            String username = auth.getName();
            String owner = invoice != null && invoice.getContract() != null && invoice.getContract().getTenant() != null
                    ? invoice.getContract().getTenant().getUsername()
                    : null;
            if (owner == null || username == null || !owner.equalsIgnoreCase(username)) {
                throw new AccessDeniedException("Không có quyền truy cập hóa đơn này");
            }
        } catch (AccessDeniedException ex) {
            throw ex;
        } catch (Exception ignored) {
            // If anything unexpected happens, do not grant access implicitly
            throw new AccessDeniedException("Không có quyền truy cập hóa đơn này");
        }
    }

    private boolean isTenantAuthenticated() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getAuthorities() == null) return false;
            return auth.getAuthorities().stream().anyMatch(a -> "ROLE_TENANT".equals(a.getAuthority()));
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean hasRole(String roleName) {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getAuthorities() == null) return false;
            String expected = roleName != null && roleName.startsWith("ROLE_") ? roleName : ("ROLE_" + roleName);
            return auth.getAuthorities().stream().anyMatch(a -> expected.equals(a.getAuthority()));
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isManagerAuthenticated() {
        return hasRole("MANAGER");
    }

    private String currentUsername() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String getMyBranchCodeForEmployee() {
        String username = currentUsername();
        if (username == null) {
            throw new AccessDeniedException("Không xác định người dùng");
        }
        Employees e = employeeRepository.findByUsername(username)
                .or(() -> employeeRepository.findByUsernameIgnoreCase(username))
                .orElseThrow(() -> new AccessDeniedException("Không tìm thấy nhân viên"));
        if (e.getBranch() == null || e.getBranch().getBranchCode() == null) {
            throw new IllegalStateException("Nhân viên chưa được gán chi nhánh");
        }
        return e.getBranch().getBranchCode();
    }

    private void assertManagerInSameBranchIfManager(Invoice invoice) {
        if (!isManagerAuthenticated()) return;
        String myBranch = getMyBranchCodeForEmployee();
        String invBranch = invoice != null && invoice.getContract() != null ? invoice.getContract().getBranchCode() : null;
        if (invBranch == null || myBranch == null || !invBranch.equalsIgnoreCase(myBranch)) {
            throw new AccessDeniedException("Không có quyền truy cập hóa đơn của chi nhánh khác");
        }
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.CREATE_INVOICE, targetType = "INVOICE", description = "Tạo hóa đơn hàng tháng (bulk)")
    public MonthlyInvoiceGenerateResponse generateMonthlyInvoices(MonthlyInvoiceGenerateRequest request) {
        if (request == null || request.getYear() == null || request.getMonth() == null) {
            throw new IllegalArgumentException("Thiếu year/month");
        }

        int year = request.getYear();
        int month = request.getMonth();
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month không hợp lệ");
        }

        LocalDate periodStart = LocalDate.of(year, month, 1);
        // Demo/test friendly default: 5 days from the time invoice is generated/sent
        LocalDate dueDate = request.getDueDate() != null
            ? request.getDueDate()
            : LocalDate.now().plusDays(5);

        List<Contract> activeContracts = contractRepository.findByStatus(ContractStatus.ACTIVE);

        int created = 0;
        int skipped = 0;
        List<Long> createdIds = new java.util.ArrayList<>();

        for (Contract c : activeContracts) {
            if (c == null || c.getId() == null) continue;

            // Skip if contract ended before the billing period
            if (c.getEndDate() != null && c.getEndDate().isBefore(periodStart)) {
                continue;
            }

            if (invoiceRepository.existsByContract_IdAndBillingYearAndBillingMonth(c.getId(), year, month)) {
                skipped++;
                continue;
            }

            InvoiceRequest invoiceRequest = new InvoiceRequest();
            invoiceRequest.setContractId(c.getId());
            invoiceRequest.setBillingYear(year);
            invoiceRequest.setBillingMonth(month);
            invoiceRequest.setDueDate(dueDate);
            InvoiceResponse resp = create(invoiceRequest);
            if (resp != null && resp.getId() != null) {
                created++;
                createdIds.add(resp.getId());
            }
        }

        return MonthlyInvoiceGenerateResponse.builder()
                .totalActiveContracts(activeContracts.size())
                .createdCount(created)
                .skippedExistingCount(skipped)
                .createdInvoiceIds(createdIds)
                .build();
    }

    @Override
    public List<ContractMonthlyInvoicePreviewResponse> previewMonthlyInvoices(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month không hợp lệ");
        }

        LocalDate periodStart = LocalDate.of(year, month, 1);
        LocalDate dueDate = LocalDate.now().plusDays(5);

        List<Contract> activeContracts = contractRepository.findByStatus(ContractStatus.ACTIVE);
        List<ContractMonthlyInvoicePreviewResponse> rows = new ArrayList<>();

        for (Contract c : activeContracts) {
            if (c == null || c.getId() == null) continue;
            if (c.getEndDate() != null && c.getEndDate().isBefore(periodStart)) continue;

            try {
                List<InvoiceDetailResponse> details = computeInvoiceDetailsPreview(c, year, month, dueDate);
                BigDecimal total = details.stream()
                        .map(InvoiceDetailResponse::getAmount)
                        .filter(v -> v != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                rows.add(ContractMonthlyInvoicePreviewResponse.builder()
                        .contractId(c.getId())
                        .branchCode(c.getBranchCode())
                        .roomNumber(c.getRoomNumber())
                        .tenantName(safeTenantName(c.getTenant()))
                        .tenantUsername(c.getTenant() != null ? c.getTenant().getUsername() : null)
                        .billingYear(year)
                        .billingMonth(month)
                        .dueDate(dueDate)
                        .amount(total)
                        .details(details)
                        .error(null)
                        .build());
            } catch (Exception ex) {
                rows.add(ContractMonthlyInvoicePreviewResponse.builder()
                        .contractId(c.getId())
                        .branchCode(c.getBranchCode())
                        .roomNumber(c.getRoomNumber())
                        .tenantName(safeTenantName(c.getTenant()))
                        .tenantUsername(c.getTenant() != null ? c.getTenant().getUsername() : null)
                        .billingYear(year)
                        .billingMonth(month)
                        .dueDate(dueDate)
                        .amount(BigDecimal.ZERO)
                        .details(java.util.List.of())
                        .error(ex.getMessage())
                        .build());
            }
        }

        return rows;
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.CREATE_INVOICE, targetType = "INVOICE", description = "Tạo hóa đơn hàng tháng (1 hợp đồng)")
    public InvoiceResponse generateMonthlyInvoiceForContract(Long contractId, MonthlyInvoiceGenerateRequest request) {
        if (contractId == null) {
            throw new IllegalArgumentException("Thiếu contractId");
        }
        if (request == null || request.getYear() == null || request.getMonth() == null) {
            throw new IllegalArgumentException("Thiếu year/month");
        }

        int year = request.getYear();
        int month = request.getMonth();
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month không hợp lệ");
        }

        LocalDate periodStart = LocalDate.of(year, month, 1);
        LocalDate dueDate = request.getDueDate() != null
            ? request.getDueDate()
            : LocalDate.now().plusDays(5);

        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hợp đồng"));
        if (c.getStatus() != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Hợp đồng không ở trạng thái ACTIVE");
        }
        if (c.getEndDate() != null && c.getEndDate().isBefore(periodStart)) {
            throw new IllegalStateException("Hợp đồng đã kết thúc trước kỳ hóa đơn");
        }
        if (invoiceRepository.existsByContract_IdAndBillingYearAndBillingMonth(contractId, year, month)) {
            throw new IllegalStateException("Hóa đơn tháng " + month + "/" + year + " đã tồn tại cho hợp đồng này");
        }

        InvoiceRequest invoiceRequest = new InvoiceRequest();
        invoiceRequest.setContractId(contractId);
        invoiceRequest.setBillingYear(year);
        invoiceRequest.setBillingMonth(month);
        invoiceRequest.setDueDate(dueDate);
        return create(invoiceRequest);
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.CREATE_INVOICE, targetType = "INVOICE", description = "Tạo hóa đơn")
    public InvoiceResponse create(InvoiceRequest request) {
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hợp đồng"));

        LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() : LocalDate.now().plusDays(5);

        Integer reqYear = request.getBillingYear();
        Integer reqMonth = request.getBillingMonth();
        int billingYear = (reqYear != null) ? reqYear : dueDate.getYear();
        int billingMonth = (reqMonth != null) ? reqMonth : dueDate.getMonthValue();
        if (billingMonth < 1 || billingMonth > 12) {
            throw new IllegalArgumentException("billingMonth không hợp lệ");
        }
        LocalDate periodStart = LocalDate.of(billingYear, billingMonth, 1);
        LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);

        if (invoiceRepository.existsByContract_IdAndBillingYearAndBillingMonth(contract.getId(), billingYear, billingMonth)) {
            throw new IllegalStateException("Hóa đơn tháng " + billingMonth + "/" + billingYear + " đã tồn tại cho hợp đồng này");
        }
    
        Invoice inv = Invoice.builder()
                .contract(contract)
                .dueDate(dueDate)
            .billingYear(billingYear)
            .billingMonth(billingMonth)
                .status(InvoiceStatus.UNPAID)
                .amount(BigDecimal.ZERO)
                .build();
    
        Invoice savedInvoice = invoiceRepository.save(inv);
    
        List<InvoiceDetail> details = new ArrayList<>();
    
        // 1. Thêm tiền phòng
        if (contract.getRoom() != null) {
            InvoiceDetail roomDetail = InvoiceDetail.builder()
                    .invoice(savedInvoice)
                    .description("Tiền phòng tháng " + billingMonth + "/" + billingYear)
                    .unitPrice(contract.getRoom().getPrice())
                    .quantity(1)
                    .amount(contract.getRoom().getPrice())
                    .build();
            details.add(roomDetail);
        }
    
        // 2. Dịch vụ gắn với hợp đồng (chỉ tính dịch vụ đang hiệu lực trong kỳ)
        for (ContractService cs : contract.getServices()) {
            if (cs == null || cs.getService() == null) continue;

            // Apply start/end date window
            LocalDate csStart = cs.getStartDate();
            LocalDate csEnd = cs.getEndDate();
            boolean activeInPeriod = (csStart == null || !csStart.isAfter(periodEnd))
                    && (csEnd == null || !csEnd.isBefore(periodStart));
            if (!activeInPeriod) {
                continue;
            }

            RentalServiceItem service = cs.getService();
            BigDecimal unitPrice = service.getPrice();
            Integer quantity = cs.getQuantity() != null ? cs.getQuantity() : 1;

            boolean isElectricity = service.getServiceName() != null && service.getServiceName().equalsIgnoreCase("Điện");
            boolean isWater = service.getServiceName() != null && service.getServiceName().equalsIgnoreCase("Nước");
        
            // Nếu là điện/nước thì tính theo chỉ số công tơ (bắt buộc có dữ liệu)
            if (isElectricity || isWater) {
                if (cs.getPreviousReading() == null || cs.getCurrentReading() == null) {
                    throw new IllegalStateException("Thiếu chỉ số " + service.getServiceName() + " cho hợp đồng #" + contract.getId() + " trong kỳ " + billingMonth + "/" + billingYear);
                }

                BigDecimal usage = cs.getCurrentReading().subtract(cs.getPreviousReading());
                if (usage.signum() < 0) {
                    throw new IllegalStateException("Chỉ số " + service.getServiceName() + " không hợp lệ (current < previous) cho hợp đồng #" + contract.getId());
                }
                quantity = usage.intValue();

                // cập nhật previousReading cho tháng sau và persist
                cs.setPreviousReading(cs.getCurrentReading());
                contractServiceRepository.save(cs);
            }
        
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        
            InvoiceDetail d = InvoiceDetail.builder()
                    .invoice(savedInvoice)
                    .description(service.getServiceName() + " tháng " +
                        billingMonth + "/" + billingYear)
                    .unitPrice(unitPrice)
                    .quantity(quantity)
                    .amount(amount)
                    .build();
        
            details.add(d);
        }

        // 3. Dịch vụ theo lịch (ví dụ: vệ sinh theo tuần) - tính số lần yêu cầu trong kỳ
        try {
            var billableBookings = serviceBookingRepository.findByContract_IdAndStatusInAndBookingDateBetween(
                contract.getId(),
                java.util.List.of(
                    com.example.rental.entity.ServiceBookingStatus.BOOKED,
                    com.example.rental.entity.ServiceBookingStatus.COMPLETED
                ),
                periodStart,
                periodEnd
            );

            if (billableBookings != null && !billableBookings.isEmpty()) {
                // group by service
            java.util.Map<Long, java.util.List<com.example.rental.entity.ServiceBooking>> byService = billableBookings.stream()
                        .filter(b -> b != null && b.getService() != null && b.getService().getId() != null)
                        .collect(java.util.stream.Collectors.groupingBy(b -> b.getService().getId()));

                for (var entry : byService.entrySet()) {
                    var bookings = entry.getValue();
                    if (bookings == null || bookings.isEmpty()) continue;
                    var service = bookings.get(0).getService();
                    int qty = bookings.size();
                    java.math.BigDecimal unitPrice = service.getPrice() != null ? service.getPrice() : java.math.BigDecimal.ZERO;
                    java.math.BigDecimal amount = unitPrice.multiply(java.math.BigDecimal.valueOf(qty));

                    InvoiceDetail d = InvoiceDetail.builder()
                            .invoice(savedInvoice)
                            .description(service.getServiceName() + " (" + qty + " lần) tháng " + billingMonth + "/" + billingYear)
                            .unitPrice(unitPrice)
                            .quantity(qty)
                            .amount(amount)
                            .build();
                    details.add(d);
                }
            }
        } catch (Exception ignored) {
            // keep invoice creation resilient if booking module not used
        }
    
        BigDecimal total = details.stream()
                .map(InvoiceDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        savedInvoice.setAmount(total);
        savedInvoice.setDetails(details);
    
        invoiceDetailRepository.saveAll(details);
        invoiceRepository.save(savedInvoice);
    
        // Gửi mail thông báo hóa đơn mới
        Tenant tenant = contract.getTenant();
        String subject = "[Rental] Hóa đơn mới #" + savedInvoice.getId();
        String html = InvoiceEmailTemplateUtil.buildNewInvoiceEmail(savedInvoice, tenant);
        emailService.sendHtmlMessage(tenant.getEmail(), subject, html);
    
        return InvoiceMapper.toResponse(savedInvoice);
    }


    @Override
    public InvoiceResponse getById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));
        assertTenantOwnsInvoiceIfTenant(invoice);
        assertManagerInSameBranchIfManager(invoice);
        return InvoiceMapper.toResponse(invoice);
    }

    @Override
    public List<InvoiceResponse> getAll() {
        if (isManagerAuthenticated()) {
            String branchCode = getMyBranchCodeForEmployee();
            return invoiceRepository.findByContract_BranchCode(branchCode).stream()
                    .map(InvoiceMapper::toResponse)
                    .collect(Collectors.toList());
        }
        return invoiceRepository.findAll().stream().map(InvoiceMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public org.springframework.data.domain.Page<InvoiceResponse> getAll(org.springframework.data.domain.Pageable pageable) {
        if (isManagerAuthenticated()) {
            String branchCode = getMyBranchCodeForEmployee();
            return invoiceRepository.findByContract_BranchCode(branchCode, pageable).map(InvoiceMapper::toResponse);
        }
        return invoiceRepository.findAll(pageable).map(InvoiceMapper::toResponse);
    }

    @Override
    public org.springframework.data.domain.Page<InvoiceResponse> search(
            org.springframework.data.domain.Pageable pageable,
            Integer year,
            Integer month,
            InvoiceStatus status
    ) {
        if (isManagerAuthenticated()) {
            String branchCode = getMyBranchCodeForEmployee();
            if (year != null && month != null) {
                java.time.LocalDate from = java.time.LocalDate.of(year, month, 1);
                java.time.LocalDate to = from.plusMonths(1).minusDays(1);
                return invoiceRepository.searchForBranch(branchCode, from, to, status, pageable)
                        .map(InvoiceMapper::toResponse);
            }
            return invoiceRepository.searchForBranch(branchCode, null, null, status, pageable)
                    .map(InvoiceMapper::toResponse);
        }

        boolean hasMonth = year != null && month != null;
        if (hasMonth) {
            java.time.LocalDate from = java.time.LocalDate.of(year, month, 1);
            java.time.LocalDate to = from.plusMonths(1).minusDays(1);
            if (status != null) {
                return invoiceRepository.findByDueDateBetweenAndStatus(from, to, status, pageable)
                        .map(InvoiceMapper::toResponse);
            }
            return invoiceRepository.findByDueDateBetween(from, to, pageable)
                    .map(InvoiceMapper::toResponse);
        }

        if (status != null) {
            return invoiceRepository.findByStatus(status, pageable)
                    .map(InvoiceMapper::toResponse);
        }

        return invoiceRepository.findAll(pageable)
                .map(InvoiceMapper::toResponse);
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.CONFIRM_PAYMENT, targetType = "INVOICE", description = "Ghi nhận thanh toán hóa đơn")
    public InvoiceResponse markPaid(Long id, boolean direct) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));
        assertTenantOwnsInvoiceIfTenant(invoice);
        assertManagerInSameBranchIfManager(invoice);

        // Tenants are not allowed to confirm cash payments themselves.
        if (isTenantAuthenticated() && direct) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Thanh toán tiền mặt cần kế toán xác nhận"
            );
        }
        // If already paid, return existing record (idempotent)
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            return InvoiceMapper.toResponse(invoice);
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidDate(LocalDate.now());
        invoice.setPaidDirect(direct);
        invoiceRepository.save(invoice);

        // ✅ Gửi mail xác nhận thanh toán
        Tenant tenant = invoice.getContract().getTenant();
        String subject = "[Rental] Thanh toán thành công hóa đơn #" + invoice.getId();
        String html = InvoiceEmailTemplateUtil.buildPaymentSuccessEmail(invoice, tenant);
        emailService.sendHtmlMessage(tenant.getEmail(), subject, html);

        // If this invoice is a checkout settlement invoice (linked via DamageReport), complete checkout.
        try {
            var reportOpt = damageReportRepository.findBySettlementInvoiceId(invoice.getId());
            if (reportOpt.isPresent()) {
                DamageReport report = reportOpt.get();
                CheckoutRequest cr = report.getCheckoutRequest();
                if (cr != null) {
                    // Ensure entity is managed
                    CheckoutRequest managed = checkoutRequestRepository.findById(cr.getId()).orElse(cr);
                    if (managed.getStatus() != CheckoutStatus.COMPLETED) {
                        managed.setStatus(CheckoutStatus.COMPLETED);
                        checkoutRequestRepository.save(managed);

                        Contract c = managed.getContract();
                        if (c != null) {
                            Contract managedContract = contractRepository.findById(c.getId()).orElse(c);
                            managedContract.setStatus(ContractStatus.ENDED);
                            if (managedContract.getRoom() != null) {
                                managedContract.getRoom().setStatus(RoomStatus.AVAILABLE);
                            }
                            contractRepository.save(managedContract);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // best-effort: do not block payment confirmation if completion hook fails
        }

        return InvoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse createMaintenanceInvoice(Long contractId, LocalDate dueDate, BigDecimal amount, String note) {
        if (contractId == null) {
            throw new IllegalArgumentException("Thiếu contractId");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Thiếu dueDate");
        }

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hợp đồng"));

        BigDecimal safeAmount = amount != null ? amount : BigDecimal.ZERO;
        if (safeAmount.signum() <= 0) {
            throw new IllegalArgumentException("Số tiền không hợp lệ");
        }
        if (safeAmount.scale() > 2) {
            throw new IllegalArgumentException("Số tiền không hợp lệ (tối đa 2 chữ số thập phân)");
        }
        if (safeAmount.compareTo(MAX_MONEY_DECIMAL_12_2) > 0) {
            throw new IllegalArgumentException("Số tiền vượt quá giới hạn hệ thống (tối đa 9,999,999,999.99)");
        }

        String desc = (note != null && !note.isBlank()) ? note : "Phí bảo trì";

        Invoice inv = Invoice.builder()
                .contract(contract)
                .dueDate(dueDate)
                // billingYear/billingMonth intentionally null for ad-hoc invoices
                .status(InvoiceStatus.UNPAID)
                .amount(BigDecimal.ZERO)
                .build();

        Invoice savedInvoice = invoiceRepository.save(inv);

        InvoiceDetail detail = InvoiceDetail.builder()
                .invoice(savedInvoice)
                .description(desc)
                .unitPrice(safeAmount)
                .quantity(1)
                .amount(safeAmount)
                .build();

        savedInvoice.setAmount(safeAmount);
        // IMPORTANT: must be mutable; Hibernate may update the collection
        savedInvoice.setDetails(new java.util.ArrayList<>(java.util.List.of(detail)));
        invoiceDetailRepository.save(detail);
        invoiceRepository.save(savedInvoice);

        Tenant tenant = contract.getTenant();
        if (tenant != null && tenant.getEmail() != null && !tenant.getEmail().isBlank()) {
            String subject = "[Rental] Hóa đơn phát sinh #" + savedInvoice.getId();
            String html = InvoiceEmailTemplateUtil.buildNewInvoiceEmail(savedInvoice, tenant);
            emailService.sendHtmlMessage(tenant.getEmail(), subject, html);
        }

        return InvoiceMapper.toResponse(savedInvoice);
    }

    @Override
    public void sendReminderForInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));

        Tenant tenant = invoice.getContract().getTenant();
        String subject = "[Rental] Nhắc nhở thanh toán hóa đơn #" + invoice.getId();
        String html = InvoiceEmailTemplateUtil.buildReminderEmail(invoice, tenant);

        emailService.sendHtmlMessage(tenant.getEmail(), subject, html);
    }

    @Override
    @Transactional
    public void markOverdueAndNotify(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn"));

        if (invoice.getStatus() == InvoiceStatus.UNPAID && invoice.getDueDate().isBefore(LocalDate.now())) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);

            Tenant tenant = invoice.getContract().getTenant();
            String subject = "[Rental] Hóa đơn #" + invoice.getId() + " đã quá hạn!";
            String html = InvoiceEmailTemplateUtil.buildOverdueEmail(invoice, tenant);
            emailService.sendHtmlMessage(tenant.getEmail(), subject, html);
        }
    }

    @Override
    @Transactional
    public void checkAndSendDueReminders() {
        LocalDate today = LocalDate.now();
        List<Invoice> invoices = invoiceRepository.findAll();

        for (Invoice inv : invoices) {
            if (inv.getStatus() == InvoiceStatus.UNPAID) {
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, inv.getDueDate());

                Tenant tenant = inv.getContract().getTenant();
                if (daysLeft == 7 || daysLeft == 3 || daysLeft == 1) {
                    String subject = "[Rental] Nhắc nhở thanh toán hóa đơn #" + inv.getId();
                    String html = InvoiceEmailTemplateUtil.buildReminderEmail(inv, tenant);
                    emailService.sendHtmlMessage(tenant.getEmail(), subject, html);
                }

                // Quá hạn
                if (inv.getDueDate().isBefore(today)) {
                    inv.setStatus(InvoiceStatus.OVERDUE);
                    invoiceRepository.save(inv);

                    String subject = "[Rental] Hóa đơn #" + inv.getId() + " đã quá hạn!";
                    String html = InvoiceEmailTemplateUtil.buildOverdueEmail(inv, tenant);
                    emailService.sendHtmlMessage(tenant.getEmail(), subject, html);
                }
            }
        }
    }

    @Override
    public java.util.List<com.example.rental.dto.invoice.InvoiceResponse> getInvoicesForTenant(Long tenantId) {
        java.util.List<Invoice> invoices = invoiceRepository.findByContract_Tenant_Id(tenantId);
        return invoices.stream().map(InvoiceMapper::toResponse).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public org.springframework.data.domain.Page<com.example.rental.dto.invoice.InvoiceResponse> getInvoicesForTenant(Long tenantId, org.springframework.data.domain.Pageable pageable) {
        return invoiceRepository.findByContract_Tenant_Id(tenantId, pageable).map(InvoiceMapper::toResponse);
    }
}
