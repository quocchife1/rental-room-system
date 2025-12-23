package com.example.rental.service.impl;

import com.example.rental.entity.*;
import com.example.rental.mapper.MaintenanceMapper;
import com.example.rental.dto.maintenance.*;
import com.example.rental.repository.*;
import com.example.rental.security.Audited;
import com.example.rental.service.MaintenanceRequestService;
import com.example.rental.service.InvoiceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder; // Import này quan trọng

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MaintenanceRequestServiceImpl implements MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final MaintenanceImageRepository maintenanceImageRepository;
    private final TenantRepository tenantRepository;
    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final InvoiceService invoiceService;
    private final MaintenanceMapper maintenanceMapper;

    // Đường dẫn lưu file (Relative path từ root project)
    private final Path rootLocation = Paths.get("uploads/maintenance");

    @Override
    @Transactional
    @Audited(action = AuditAction.CREATE_MAINTENANCE_REQUEST, targetType = "MAINTENANCE_REQUEST", description = "Tạo yêu cầu bảo trì")
    public MaintenanceResponse createRequest(MaintenanceRequestCreate request) {
        Tenant tenant;
        if (request.getTenantId() != null) {
            tenant = tenantRepository.findById(request.getTenantId())
                    .orElseThrow(() -> new EntityNotFoundException("Tenant not found by id"));
        } else {
            tenant = tenantRepository.findByFullName(request.getTenantName())
                    .orElseThrow(() -> new EntityNotFoundException("Tenant not found by name"));
        }

        Room room = roomRepository.findByBranchCodeAndRoomNumber(
                request.getBranchCode(), request.getRoomNumber())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        MaintenanceRequest entity = MaintenanceRequest.builder()
                .requestCode("MR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .tenant(tenant)
                .room(room)
                .description(request.getDescription())
                .status(MaintenanceStatus.PENDING)
                .build();

        MaintenanceRequest saved = maintenanceRequestRepository.save(entity);

        if (request.getImages() != null && request.getImages().length > 0) {
            List<MaintenanceImage> images = new ArrayList<>();
            try {
                // Tạo thư mục nếu chưa tồn tại
                if (!Files.exists(rootLocation)) {
                    Files.createDirectories(rootLocation);
                }

                for (MultipartFile file : request.getImages()) {
                    if (file.isEmpty()) continue;

                    // 1. Tạo tên file unique
                    String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    
                    // 2. Lưu file vật lý
                    Path destinationFile = rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();
                    try (var inputStream = file.getInputStream()) {
                        Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                    }

                    // 3. Tạo Full URL để trả về Frontend (Giống Contract)
                    // URL sẽ có dạng: http://localhost:8080/uploads/maintenance/filename.jpg
                    String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/uploads/maintenance/")
                            .path(filename)
                            .toUriString();

                    // 4. Lưu URL vào DB
                    MaintenanceImage img = MaintenanceImage.builder()
                            .imageUrl(fileUrl) 
                            .maintenanceRequest(saved)
                            .build();
                    images.add(img);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to store images", e);
            }
            
            if (!images.isEmpty()) {
                maintenanceImageRepository.saveAll(images);
                saved.setImages(images);
            }
        }

        return maintenanceMapper.toResponse(saved);
    }
    
    // ... (Giữ nguyên các method khác: getRequestsByTenant, getRequestsByStatus, updateRequest...)
    @Override
    public List<MaintenanceResponse> getRequestsByTenant(Long tenantId) {
        return maintenanceRequestRepository.findByTenantId(tenantId).stream().map(maintenanceMapper::toResponse).toList();
    }
    @Override
    public List<MaintenanceResponse> getRequestsByStatus(String status) {
        return maintenanceRequestRepository.findByStatus(MaintenanceStatus.valueOf(status)).stream().map(maintenanceMapper::toResponse).toList();
    }

    @Override
    public List<MaintenanceResponse> getAllRequests() {
        return maintenanceRequestRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(MaintenanceRequest::getId).reversed())
                .map(maintenanceMapper::toResponse)
                .toList();
    }
    @Override
    @Transactional
    @Audited(action = AuditAction.UPDATE_MAINTENANCE_STATUS, targetType = "MAINTENANCE_REQUEST", description = "Cập nhật xử lý bảo trì")
    public MaintenanceResponse updateRequest(Long requestId, String resolution, String status, String technician, String cost) {
        MaintenanceRequest entity = maintenanceRequestRepository.findById(requestId).orElseThrow(() -> new EntityNotFoundException("Not found"));
        if (resolution != null) entity.setResolution(resolution);
        if (technician != null) entity.setTechnicianName(technician);
        if (cost != null) entity.setCost(new BigDecimal(cost));
        if (status != null) entity.setStatus(MaintenanceStatus.valueOf(status));
        return maintenanceMapper.toResponse(maintenanceRequestRepository.save(entity));
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.UPDATE_MAINTENANCE_STATUS, targetType = "MAINTENANCE_REQUEST", description = "Cập nhật trạng thái bảo trì")
    public MaintenanceResponse updateStatus(Long requestId, String status) {
        MaintenanceRequest entity = maintenanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Not found"));
        if (status != null) entity.setStatus(MaintenanceStatus.valueOf(status));
        return maintenanceMapper.toResponse(maintenanceRequestRepository.save(entity));
    }

    @Override
    @Transactional
    @Audited(action = AuditAction.CREATE_INVOICE, targetType = "INVOICE", description = "Tạo hóa đơn bảo trì do lỗi người thuê")
    public MaintenanceInvoiceCreateResponse createTenantFaultInvoice(Long requestId, MaintenanceInvoiceCreateRequest request) {
        MaintenanceRequest entity = maintenanceRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Not found"));

        if (entity.getInvoiceId() != null) {
            throw new IllegalStateException("Yêu cầu này đã có hóa đơn phát sinh");
        }

        java.math.BigDecimal amount = request != null ? request.getAmount() : null;
        if (amount == null) {
            amount = entity.getCost();
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Thiếu hoặc số tiền không hợp lệ");
        }

        java.time.LocalDate dueDate = request != null ? request.getDueDate() : null;
        if (dueDate == null) {
            dueDate = java.time.LocalDate.now().plusDays(7);
        }

        String note = request != null ? request.getNote() : null;
        if (note == null || note.isBlank()) {
            note = "Phí bảo trì (" + entity.getRequestCode() + ")";
        }

        if (entity.getRoom() == null || entity.getRoom().getId() == null) {
            throw new IllegalStateException("Thiếu thông tin phòng");
        }

        Contract contract = contractRepository.findByRoomIdAndStatus(entity.getRoom().getId(), ContractStatus.ACTIVE);
        if ((contract == null || contract.getId() == null) && entity.getTenant() != null && entity.getTenant().getId() != null) {
            // Nếu chưa ACTIVE (ví dụ: SIGNED_PENDING_DEPOSIT) thì fallback theo hợp đồng gần nhất của đúng tenant
            contract = contractRepository.findTopByRoomIdAndTenantIdOrderByCreatedAtDesc(
                    entity.getRoom().getId(),
                    entity.getTenant().getId()
            );
        }
        if (contract == null || contract.getId() == null) {
            throw new IllegalStateException("Không tìm thấy hợp đồng phù hợp cho phòng/người thuê này");
        }

        if (entity.getTenant() != null && contract.getTenant() != null
                && entity.getTenant().getId() != null && contract.getTenant().getId() != null
                && !entity.getTenant().getId().equals(contract.getTenant().getId())) {
            throw new IllegalStateException("Hợp đồng ACTIVE không thuộc về người thuê của yêu cầu này");
        }

        var invoice = invoiceService.createMaintenanceInvoice(contract.getId(), dueDate, amount, note);
        entity.setInvoiceId(invoice.getId());
        MaintenanceRequest saved = maintenanceRequestRepository.save(entity);

        return MaintenanceInvoiceCreateResponse.builder()
                .maintenance(maintenanceMapper.toResponse(saved))
                .invoice(invoice)
                .build();
    }
}