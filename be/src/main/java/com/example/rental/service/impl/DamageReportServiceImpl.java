package com.example.rental.service.impl;

import com.example.rental.dto.damage.DamageImageDto;
import com.example.rental.dto.damage.DamageReportCreateRequest;
import com.example.rental.dto.damage.DamageReportResponse;
import com.example.rental.entity.*;
import com.example.rental.repository.*;
import com.example.rental.service.DamageReportService;
import jakarta.persistence.EntityNotFoundException;
import com.example.rental.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DamageReportServiceImpl implements DamageReportService {

    private final DamageReportRepository damageReportRepository;
    private final DamageImageRepository damageImageRepository;
    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogRepository auditLogRepository;

    private static final Path UPLOAD_PATH = Paths.get(System.getProperty("user.dir"), "uploads", "damage");

    @Override
    public DamageReportResponse createDamageReport(DamageReportCreateRequest request, List<MultipartFile> images)
            throws IOException {
        // Kiểm tra contractId
        if (request.getContractId() == null) {
            throw new BadRequestException(
                    "Thiếu trường 'contractId'. Vui lòng chọn hợp đồng/ phòng trước khi tạo báo cáo.");
        }

        // Tìm hợp đồng
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hợp đồng"));

        // Lấy nhân viên kiểm tra từ SecurityContext (người đang đăng nhập)
        Employees inspector = null;
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            if (username != null) {
                inspector = employeeRepository.findByUsername(username).orElse(null);
            }
        } catch (Exception e) {
            log.warn("Không lấy được thông tin inspector từ SecurityContext: {}", e.getMessage());
        }

        if (inspector == null) {
            throw new IllegalStateException(
                    "Không tìm thấy nhân viên (inspector). Vui lòng đăng nhập bằng tài khoản nhân viên.");
        }

        DamageReport damageReport = DamageReport.builder()
                .contract(contract)
                .inspector(inspector)
                .description(request.getDescription())
                .damageDetails(request.getDamageDetails())
                .totalDamageCost(request.getTotalDamageCost() != null ? request.getTotalDamageCost() : BigDecimal.ZERO)
                .status(DamageReportStatus.DRAFT)
                .build();

        DamageReport saved = damageReportRepository.save(damageReport);

        // Upload hình ảnh nếu có
        if (images != null && !images.isEmpty()) {
            uploadDamageImages(saved.getId(), images);
        }

        log.info("✓ Tạo báo cáo hư hỏng thành công: ID={}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DamageReport> findById(Long id) {
        return damageReportRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DamageReportResponse getById(Long id) {
        DamageReport damageReport = damageReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy báo cáo hư hỏng"));
        return toResponse(damageReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DamageReportResponse> getByContractId(Long contractId) {
        return damageReportRepository.findAll().stream()
                .filter(dr -> dr.getContract().getId().equals(contractId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DamageReportResponse> getByStatus(String status) {
        try {
            DamageReportStatus reportStatus = DamageReportStatus.valueOf(status.toUpperCase());
            return damageReportRepository.findAll().stream()
                    .filter(dr -> dr.getStatus() == reportStatus)
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DamageReportResponse> getAll() {
        return damageReportRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DamageReportResponse updateDamageReport(Long id, DamageReportCreateRequest request,
            List<MultipartFile> newImages) throws IOException {
        DamageReport damageReport = damageReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy báo cáo hư hỏng"));

        // Chỉ cho phép chỉnh sửa nếu ở trạng thái DRAFT
        if (damageReport.getStatus() != DamageReportStatus.DRAFT) {
            throw new IllegalStateException("Không thể chỉnh sửa báo cáo ở trạng thái: " + damageReport.getStatus());
        }

        // Cập nhật thông tin
        damageReport.setDescription(request.getDescription());
        damageReport.setDamageDetails(request.getDamageDetails());
        damageReport.setTotalDamageCost(
                request.getTotalDamageCost() != null ? request.getTotalDamageCost() : BigDecimal.ZERO);

        DamageReport updated = damageReportRepository.save(damageReport);

        // Thêm hình ảnh mới nếu có
        if (newImages != null && !newImages.isEmpty()) {
            uploadDamageImages(id, newImages);
        }

        log.info("✓ Cập nhật báo cáo hư hỏng thành công: ID={}", id);
        return toResponse(updated);
    }

    @Override
    public DamageReportResponse submitForApproval(Long id) {
        DamageReport damageReport = damageReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy báo cáo hư hỏng"));

        if (damageReport.getStatus() != DamageReportStatus.DRAFT) {
            throw new IllegalStateException("Chỉ có thể gửi báo cáo ở trạng thái DRAFT");
        }

        damageReport.setStatus(DamageReportStatus.SUBMITTED);
        DamageReport updated = damageReportRepository.save(damageReport);

        log.info("✓ Gửi báo cáo hư hỏng để duyệt: ID={}", id);
        return toResponse(updated);
    }

    @Override
    public DamageReportResponse approveDamageReport(Long id, String approverNote) {
        DamageReport damageReport = damageReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy báo cáo hư hỏng"));

        if (damageReport.getStatus() != DamageReportStatus.SUBMITTED) {
            throw new IllegalStateException("Chỉ có thể phê duyệt báo cáo ở trạng thái SUBMITTED");
        }

        damageReport.setStatus(DamageReportStatus.APPROVED);
        damageReport.setApproverNote(approverNote);
        damageReport.setApprovedAt(LocalDateTime.now());

        DamageReport updated = damageReportRepository.save(damageReport);

        log.info("✓ Phê duyệt báo cáo hư hỏng: ID={}", id);
        return toResponse(updated);
    }

    @Override
    public DamageReportResponse rejectDamageReport(Long id, String rejectReason) {
        DamageReport damageReport = damageReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy báo cáo hư hỏng"));

        if (damageReport.getStatus() != DamageReportStatus.SUBMITTED) {
            throw new IllegalStateException("Chỉ có thể từ chối báo cáo ở trạng thái SUBMITTED");
        }

        damageReport.setStatus(DamageReportStatus.REJECTED);
        damageReport.setApproverNote(rejectReason);

        DamageReport updated = damageReportRepository.save(damageReport);

        log.info("✓ Từ chối báo cáo hư hỏng: ID={}", id);
        return toResponse(updated);
    }

    @Override
    public void deleteDamageReport(Long id) {
        DamageReport damageReport = damageReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy báo cáo hư hỏng"));

        if (damageReport.getStatus() != DamageReportStatus.DRAFT) {
            throw new IllegalStateException("Chỉ có thể xóa báo cáo ở trạng thái DRAFT");
        }

        // Xóa hình ảnh liên quan
        damageImageRepository.deleteAll(damageReport.getImages());
        damageReportRepository.delete(damageReport);

        log.info("✓ Xóa báo cáo hư hỏng: ID={}", id);
    }

    @Override
    public String uploadDamageImages(Long damageReportId, List<MultipartFile> images) throws IOException {
        DamageReport damageReport = damageReportRepository.findById(damageReportId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy báo cáo hư hỏng"));

        // Tạo thư mục uploads/damage trong project nếu chưa có
        Files.createDirectories(UPLOAD_PATH);

        List<DamageImage> savedImages = new ArrayList<>();

        for (MultipartFile file : images) {
            try {
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path destPath = UPLOAD_PATH.resolve(filename);
                File dest = destPath.toFile();
                // đảm bảo thư mục đích tồn tại
                Files.createDirectories(destPath.getParent());
                file.transferTo(dest);

                DamageImage damageImage = DamageImage.builder()
                        .damageReport(damageReport)
                        .imageUrl("/uploads/damage/" + filename)
                        .description(file.getOriginalFilename())
                        .build();

                savedImages.add(damageImage);
            } catch (IOException e) {
                log.error("Lỗi khi upload hình ảnh: {}", e.getMessage());
                throw e;
            }
        }

        damageImageRepository.saveAll(savedImages);
        damageReport.setImages(savedImages);
        damageReportRepository.save(damageReport);

        log.info("✓ Tải lên {} hình ảnh cho báo cáo hư hỏng: ID={}", savedImages.size(), damageReportId);
        return "Tải lên " + savedImages.size() + " hình ảnh thành công";
    }

    private DamageReportResponse toResponse(DamageReport damageReport) {
        DamageReportResponse response = new DamageReportResponse();
        response.setId(damageReport.getId());
        response.setCheckoutRequestId(damageReport.getCheckoutRequest() != null ? damageReport.getCheckoutRequest().getId() : null);
        response.setContractId(damageReport.getContract().getId());
        response.setContractCode("C-" + damageReport.getContract().getId());
        response.setTenantName(
                damageReport.getContract().getTenant() != null ? damageReport.getContract().getTenant().getFullName()
                        : "N/A");
        response.setRoomCode(damageReport.getContract().getRoom().getRoomCode());
        response.setInspectorName(
                damageReport.getInspector() != null ? damageReport.getInspector().getFullName() : "N/A");
        response.setDescription(damageReport.getDescription());
        response.setDamageDetails(damageReport.getDamageDetails());
        response.setTotalDamageCost(damageReport.getTotalDamageCost());
        response.setSettlementInvoiceId(damageReport.getSettlementInvoiceId());
        response.setStatus(damageReport.getStatus().name());
        response.setApproverName(damageReport.getApprover() != null ? damageReport.getApprover().getFullName() : null);
        response.setApproverNote(damageReport.getApproverNote());
        response.setCreatedAt(damageReport.getCreatedAt());
        response.setApprovedAt(damageReport.getApprovedAt());

        if (damageReport.getImages() != null) {
            response.setImages(damageReport.getImages().stream()
                    .map(img -> {
                        DamageImageDto dto = new DamageImageDto();
                        dto.setId(img.getId());
                        dto.setImageUrl(img.getImageUrl());
                        dto.setDescription(img.getDescription());
                        return dto;
                    })
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
