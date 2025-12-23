package com.example.rental.service.impl;

import com.example.rental.dto.contract.ContractCreateRequest;
import com.example.rental.dto.contract.DepositPaymentRequest;
import com.example.rental.dto.contract.ContractUpdateRequest;
import com.example.rental.entity.*;
import com.example.rental.repository.*;
import com.example.rental.service.ContractService;
import com.example.rental.utils.ContractDocxGenerator;
import com.example.rental.utils.DepositDocxGenerator;
import com.example.rental.utils.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;
    private final BranchRepository branchRepository;
    private final FileStorageService fileStorageService;
    private final ContractDocxGenerator contractDocxGenerator;
    private final DepositDocxGenerator depositDocxGenerator;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;
    private final GuestRepository guestRepository;
    private final SystemConfigRepository systemConfigRepository;
        private final ContractServiceRepository contractServiceRepository;
        private final RentalServiceRepository rentalServiceRepository;

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
                // readings must be entered by manager before invoicing
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

    private void deleteGuestAccountIfExistsForTenant(Tenant tenant) {
        if (tenant == null || tenant.getUsername() == null || tenant.getUsername().trim().isEmpty()) {
            return;
        }
        String username = tenant.getUsername().trim();
        guestRepository.findByUsername(username)
                .or(() -> guestRepository.findByUsernameIgnoreCase(username))
                .ifPresent(guestRepository::delete);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        String wanted1 = "ROLE_" + role;
        String wanted2 = role;
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (a == null || a.getAuthority() == null) continue;
            String v = a.getAuthority();
            if (wanted1.equalsIgnoreCase(v) || wanted2.equalsIgnoreCase(v)) return true;
        }
        return false;
    }

    private String getCurrentEmployeeBranchCode() {
        String username = getCurrentUsername();
        if (username == null) throw new UsernameNotFoundException("Unauthenticated");

        Employees emp = employeeRepository.findByUsername(username)
                .or(() -> employeeRepository.findByUsernameIgnoreCase(username))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên."));

        if (emp.getBranch() == null || emp.getBranch().getBranchCode() == null) {
            throw new RuntimeException("Nhân viên chưa được gán chi nhánh.");
        }

        return emp.getBranch().getBranchCode();
    }

    @Override
    @Transactional
    @com.example.rental.security.Audited(action = com.example.rental.entity.AuditAction.CREATE_CONTRACT, targetType = "CONTRACT", description = "Tạo hợp đồng mới")
    public Contract createContract(ContractCreateRequest request) throws IOException {
        String requestBranch = request.getBranchCode();
        String effectiveBranch = requestBranch;

        // Nhân viên (không phải ADMIN): chỉ được lập hợp đồng cho chi nhánh của mình
        if (!hasRole("ADMIN")) {
            String username = getCurrentUsername();
            if (username != null) {
                Optional<Employees> empOpt = employeeRepository.findByUsername(username)
                        .or(() -> employeeRepository.findByUsernameIgnoreCase(username));
                if (empOpt.isPresent()) {
                    Employees emp = empOpt.get();
                    String empBranch = emp.getBranch() != null ? emp.getBranch().getBranchCode() : null;
                    if (empBranch == null || empBranch.trim().isEmpty()) {
                        throw new RuntimeException("Nhân viên chưa được gán chi nhánh.");
                    }
                    if (requestBranch != null && !requestBranch.trim().isEmpty() && !empBranch.equalsIgnoreCase(requestBranch.trim())) {
                        throw new RuntimeException("Bạn chỉ có thể lập hợp đồng cho chi nhánh của mình: " + empBranch);
                    }
                    effectiveBranch = empBranch;
                }
            }
        }

        if (effectiveBranch == null || effectiveBranch.trim().isEmpty()) {
            throw new RuntimeException("Thiếu mã chi nhánh.");
        }
        final String branchCode = effectiveBranch.trim();
        if (request.getRoomNumber() == null || request.getRoomNumber().trim().isEmpty()) {
            throw new RuntimeException("Thiếu số phòng.");
        }

        Branch branch = branchRepository.findByBranchCode(branchCode)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh: " + branchCode));

        Room room = roomRepository.findByBranchCodeAndRoomNumber(branchCode, request.getRoomNumber())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng " + request.getRoomNumber() + " tại chi nhánh " + branchCode));

        if (contractRepository.findByRoomIdAndStatus(room.getId(), ContractStatus.ACTIVE) != null)
            throw new RuntimeException("Phòng này đang có hợp đồng hoạt động!");

        Tenant tenant = getOrCreateTenant(request);

        Contract contract = Contract.builder()
                .tenant(tenant)
                .room(room)
            .branchCode(branch.getBranchCode())
                .roomNumber(room.getRoomNumber())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
            .endDate(request.getEndDate())
                .deposit(request.getDeposit() != null ? request.getDeposit() : BigDecimal.ZERO)
                .status(ContractStatus.PENDING)
                .build();

        Contract saved = contractRepository.save(contract);

        // Mark room as rented immediately when contract is created
        if (room != null) {
            room.setStatus(com.example.rental.entity.RoomStatus.OCCUPIED);
            roomRepository.save(room);
        }

        // ✅ Sinh file Word hợp đồng vào thư mục riêng
        String docxPath = contractDocxGenerator.generateContractFile(saved, request);
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(docxPath).toUriString();

        saved.setContractFileUrl(fileUrl); // link tải xuống trực tiếp

        // Nếu user từng là GUEST, xóa account Guest để chuyển sang TENANT khi đăng nhập
        deleteGuestAccountIfExistsForTenant(tenant);
        return contractRepository.save(saved);
    }

    private Tenant getOrCreateTenant(ContractCreateRequest request) {
        if (request.getTenantId() != null)
            return tenantRepository.findById(request.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người thuê"));

        if (tenantRepository.existsByEmail(request.getTenantEmail()))
            throw new RuntimeException("Email đã tồn tại");
        if (tenantRepository.existsByCccd(request.getTenantCccd()))
            throw new RuntimeException("CCCD đã tồn tại");
        if (tenantRepository.existsByStudentId(request.getStudentId()))
            throw new RuntimeException("Mã sinh viên đã tồn tại");

        String normalizedUsername = request.getTenantEmail() != null ? request.getTenantEmail().trim().toLowerCase() : null;

        Tenant tenant = Tenant.builder()
            .username(normalizedUsername)
            .password(passwordEncoder.encode("123456"))
                .fullName(request.getTenantFullName())
                .email(request.getTenantEmail())
                .phoneNumber(request.getTenantPhoneNumber())
                .address(request.getTenantAddress())
                .cccd(request.getTenantCccd())
                .studentId(request.getStudentId())
                .university(request.getUniversity())
                .status(UserStatus.ACTIVE)
                .build();

        return tenantRepository.save(tenant);
    }

    @Override
    public List<Contract> findAll() {
        return contractRepository.findAll();
    }

    @Override
    public java.util.List<Contract> findByTenantId(Long tenantId) {
        return contractRepository.findByTenantId(tenantId);
    }

    @Override
    public org.springframework.data.domain.Page<Contract> findByTenantId(Long tenantId, org.springframework.data.domain.Pageable pageable) {
        return contractRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    public Optional<Contract> findById(Long id) {
        return contractRepository.findById(id);
    }

    @Override
    @Transactional
    @com.example.rental.security.Audited(action = com.example.rental.entity.AuditAction.SIGN_CONTRACT, targetType = "CONTRACT", description = "Upload hợp đồng đã ký")
    public Contract uploadSignedContract(Long id, MultipartFile file) throws IOException {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng"));

        if (contract.getStatus() == null || contract.getStatus() != ContractStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể upload hợp đồng đã ký khi hợp đồng còn ở trạng thái PENDING.");
        }

        String filename = fileStorageService.storeFile(file, "contracts");
        String fileUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/contracts/")
                .path(filename)
                .toUriString();

        contract.setSignedContractUrl(fileUri);
        // Upload xong vẫn CHƯA kích hoạt: chờ thanh toán tiền cọc
        contract.setStatus(ContractStatus.SIGNED_PENDING_DEPOSIT);

        return contractRepository.save(contract);
    }

    @Override
    @Transactional
    @com.example.rental.security.Audited(action = com.example.rental.entity.AuditAction.UPDATE_CONTRACT, targetType = "CONTRACT", description = "Xác nhận thanh toán tiền cọc và kích hoạt hợp đồng")
    public Contract confirmDepositPaymentForStaff(Long id, DepositPaymentRequest request) throws IOException {
        Contract contract = getContractForStaff(id);

        if (contract.getStatus() == null || contract.getStatus() != ContractStatus.SIGNED_PENDING_DEPOSIT) {
            throw new RuntimeException("Hợp đồng chưa ở trạng thái chờ thanh toán tiền cọc.");
        }

        if (request == null || request.getMethod() == null) {
            throw new RuntimeException("Vui lòng chọn phương thức thanh toán (CASH hoặc BANK_TRANSFER).");
        }
        if (request.getMethod() != PaymentMethod.CASH && request.getMethod() != PaymentMethod.BANK_TRANSFER) {
            throw new RuntimeException("Phương thức thanh toán không hợp lệ. Chỉ hỗ trợ CASH hoặc BANK_TRANSFER.");
        }

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : contract.getDeposit();
        if (amount == null) amount = BigDecimal.ZERO;

        contract.setDepositPaymentMethod(request.getMethod());
        contract.setDepositPaidDate(LocalDateTime.now());
        contract.setDepositPaymentReference(request.getReference());

        // Generate 2 documents for printing
        DepositDocxGenerator.TransferInfo transferInfo = null;
        if (request.getMethod() == PaymentMethod.BANK_TRANSFER) {
            var cfg = systemConfigRepository.findById(1L).orElse(null);
            if (cfg != null) {
                transferInfo = new DepositDocxGenerator.TransferInfo(
                        cfg.getMomoReceiverName(),
                        cfg.getMomoReceiverPhone(),
                        cfg.getMomoReceiverQrUrl()
                );
            }
        }
        DepositDocxGenerator.Result docs = depositDocxGenerator.generate(contract, amount, request.getMethod(), request.getReference(), transferInfo);
        String invoiceUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(docs.getInvoicePath()).toUriString();
        String receiptUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(docs.getReceiptPath()).toUriString();
        contract.setDepositInvoiceUrl(invoiceUrl);
        contract.setDepositReceiptUrl(receiptUrl);

        // Activate contract after deposit payment
        contract.setStatus(ContractStatus.ACTIVE);

        // Auto attach electricity/water services (utilities)
        ensureUtilityServicesForContract(contract);

        // Ensure room is occupied when contract becomes ACTIVE
        if (contract.getRoom() != null) {
            contract.getRoom().setStatus(com.example.rental.entity.RoomStatus.OCCUPIED);
            roomRepository.save(contract.getRoom());
        }

        return contractRepository.save(contract);
    }

    @Override
    public Resource downloadContract(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng"));

        if (contract.getContractFileUrl() == null)
            throw new RuntimeException("Hợp đồng chưa có file đính kèm.");

        // ✅ Trỏ đúng file docx
        Path filePath = Paths.get(System.getProperty("user.dir"), "uploads/generated_contracts", "contract_" + contract.getId() + ".docx");
        File file = filePath.toFile();

        if (!file.exists()) {
            throw new RuntimeException("File hợp đồng không tồn tại: " + file.getAbsolutePath());
        }

        return new FileSystemResource(file);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contract> getMyBranchContracts(String status, String query, Pageable pageable) {
        String q = query == null ? "" : query.trim();
        String statusStr = status == null ? "" : status.trim();

        ContractStatus statusEnum = null;
        if (!statusStr.isEmpty() && !"ALL".equalsIgnoreCase(statusStr)) {
            try {
                statusEnum = ContractStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Trạng thái không hợp lệ: " + statusStr);
            }
        }

        // ADMIN: xem toàn bộ
        if (hasRole("ADMIN")) {
            if (!q.isEmpty()) {
                return contractRepository.searchGlobal(q, statusEnum, pageable);
            }
            if (statusEnum != null) {
                return contractRepository.findByStatus(statusEnum, pageable);
            }
            return contractRepository.findAll(pageable);
        }

        // MANAGER/RECEPTIONIST: chỉ xem chi nhánh của mình
        String branchCode = getCurrentEmployeeBranchCode();
        if (!q.isEmpty()) {
            return contractRepository.searchInBranch(branchCode, q, statusEnum, pageable);
        }
        if (statusEnum != null) {
            return contractRepository.findByBranchCodeAndStatus(branchCode, statusEnum, pageable);
        }
        return contractRepository.findByBranchCode(branchCode, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Contract getContractForStaff(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng."));

        if (hasRole("ADMIN")) {
            return contract;
        }

        String myBranch = getCurrentEmployeeBranchCode();
        String contractBranch = contract.getBranchCode();
        if (contractBranch == null || !contractBranch.equalsIgnoreCase(myBranch)) {
            throw new RuntimeException("Bạn không có quyền truy cập hợp đồng của chi nhánh khác.");
        }
        return contract;
    }

    @Override
    @Transactional
    @com.example.rental.security.Audited(action = com.example.rental.entity.AuditAction.UPDATE_CONTRACT, targetType = "CONTRACT", description = "Cập nhật hợp đồng (PENDING)")
    public Contract updateContractForStaff(Long id, ContractUpdateRequest request) throws IOException {
        Contract contract = getContractForStaff(id);

        if (contract.getStatus() == null || contract.getStatus() != ContractStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể chỉnh sửa hợp đồng khi còn ở trạng thái PENDING.");
        }

        Tenant tenant = contract.getTenant();
        if (tenant == null) {
            throw new RuntimeException("Hợp đồng không có thông tin người thuê.");
        }

        // Uniqueness checks (exclude current tenant)
        if (request.getTenantEmail() != null && !request.getTenantEmail().trim().isEmpty()) {
            String email = request.getTenantEmail().trim();
            if (!email.equalsIgnoreCase(tenant.getEmail()) && tenantRepository.existsByEmailAndIdNot(email, tenant.getId())) {
                throw new RuntimeException("Email đã tồn tại");
            }
            tenant.setEmail(email);
            tenant.setUsername(email.toLowerCase());
        }

        if (request.getTenantCccd() != null && !request.getTenantCccd().trim().isEmpty()) {
            String cccd = request.getTenantCccd().trim();
            if (!cccd.equalsIgnoreCase(tenant.getCccd()) && tenantRepository.existsByCccdAndIdNot(cccd, tenant.getId())) {
                throw new RuntimeException("CCCD đã tồn tại");
            }
            tenant.setCccd(cccd);
        }

        if (request.getStudentId() != null && !request.getStudentId().trim().isEmpty()) {
            String studentId = request.getStudentId().trim();
            if (!studentId.equalsIgnoreCase(tenant.getStudentId()) && tenantRepository.existsByStudentIdAndIdNot(studentId, tenant.getId())) {
                throw new RuntimeException("Mã sinh viên đã tồn tại");
            }
            tenant.setStudentId(studentId);
        }

        if (request.getTenantFullName() != null) tenant.setFullName(request.getTenantFullName());
        if (request.getTenantPhoneNumber() != null) tenant.setPhoneNumber(request.getTenantPhoneNumber());
        if (request.getTenantAddress() != null) tenant.setAddress(request.getTenantAddress());
        if (request.getUniversity() != null) tenant.setUniversity(request.getUniversity());

        tenantRepository.save(tenant);

        if (request.getDeposit() != null) contract.setDeposit(request.getDeposit());
        if (request.getStartDate() != null) contract.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) contract.setEndDate(request.getEndDate());

        Contract saved = contractRepository.save(contract);

        // Regenerate DOCX so staff downloads the updated content
        ContractCreateRequest docReq = new ContractCreateRequest();
        docReq.setBranchCode(saved.getBranchCode());
        docReq.setRoomNumber(saved.getRoomNumber());
        docReq.setTenantId(saved.getTenant() != null ? saved.getTenant().getId() : null);
        docReq.setTenantFullName(saved.getTenant() != null ? saved.getTenant().getFullName() : null);
        docReq.setTenantPhoneNumber(saved.getTenant() != null ? saved.getTenant().getPhoneNumber() : null);
        docReq.setTenantEmail(saved.getTenant() != null ? saved.getTenant().getEmail() : null);
        docReq.setTenantAddress(saved.getTenant() != null ? saved.getTenant().getAddress() : null);
        docReq.setTenantCccd(saved.getTenant() != null ? saved.getTenant().getCccd() : null);
        docReq.setStudentId(saved.getTenant() != null ? saved.getTenant().getStudentId() : null);
        docReq.setUniversity(saved.getTenant() != null ? saved.getTenant().getUniversity() : null);
        docReq.setDeposit(saved.getDeposit());
        docReq.setStartDate(saved.getStartDate());
        docReq.setEndDate(saved.getEndDate());

        String docxPath = contractDocxGenerator.generateContractFile(saved, docReq);
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(docxPath).toUriString();
        saved.setContractFileUrl(fileUrl);
        return contractRepository.save(saved);
    }

    @Override
    @Transactional
    @com.example.rental.security.Audited(action = com.example.rental.entity.AuditAction.DELETE_DATA, targetType = "CONTRACT", description = "Xóa hợp đồng tạm (PENDING)")
    public void deletePendingContractForStaff(Long id) {
        Contract contract = getContractForStaff(id);

        if (contract.getStatus() == null || contract.getStatus() != ContractStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể xóa hợp đồng tạm khi còn ở trạng thái PENDING.");
        }

        // Revert room to AVAILABLE (best-effort)
        Room room = contract.getRoom();
        if (room != null) {
            room.setStatus(com.example.rental.entity.RoomStatus.AVAILABLE);
            roomRepository.save(room);
        }

        contractRepository.delete(contract);
    }
}
