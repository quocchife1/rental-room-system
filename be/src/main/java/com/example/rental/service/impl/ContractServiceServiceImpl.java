package com.example.rental.service.impl;

import com.example.rental.dto.contractservice.ContractServiceRequest;
import com.example.rental.dto.contractservice.ContractServiceResponse;
import com.example.rental.dto.contractservice.MeterReadingUpdateRequest;
import com.example.rental.entity.Contract;
import com.example.rental.entity.ContractStatus;
import com.example.rental.entity.ContractService;
import com.example.rental.entity.RentalServiceItem;
import com.example.rental.security.Audited;
import com.example.rental.entity.AuditAction;
import com.example.rental.mapper.ContractServiceMapper;
import com.example.rental.repository.ContractRepository;
import com.example.rental.repository.ContractServiceRepository;
import com.example.rental.repository.RentalServiceRepository;
import com.example.rental.service.ContractServiceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
public class ContractServiceServiceImpl implements ContractServiceService {

    private final ContractRepository contractRepository;
    private final ContractServiceRepository contractServiceRepository;
    private final RentalServiceRepository rentalServiceRepository;

    private boolean isTenant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a != null && "ROLE_TENANT".equals(a.getAuthority()));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return auth.getName();
    }

    private void assertTenantOwnsContract(Contract contract) {
        if (!isTenant()) return;
        String username = currentUsername();
        if (username == null || contract == null || contract.getTenant() == null || contract.getTenant().getUsername() == null) {
            throw new RuntimeException("Không có quyền truy cập hợp đồng");
        }
        if (!contract.getTenant().getUsername().equalsIgnoreCase(username)) {
            throw new RuntimeException("Bạn không có quyền thao tác trên hợp đồng này");
        }
    }

        private void ensureFixedServicesForActiveContract(Contract contract) {
        if (contract == null || contract.getId() == null) return;
        if (contract.getStatus() != ContractStatus.ACTIVE) return;

        List<ContractService> existing = contractServiceRepository.findByContractId(contract.getId());
        boolean hasElectricity = existing.stream().anyMatch(cs -> cs != null && cs.getService() != null
            && cs.getService().getServiceName() != null
            && cs.getService().getServiceName().equalsIgnoreCase("Điện"));
        boolean hasWater = existing.stream().anyMatch(cs -> cs != null && cs.getService() != null
            && cs.getService().getServiceName() != null
            && cs.getService().getServiceName().equalsIgnoreCase("Nước"));
        boolean hasSecurity = existing.stream().anyMatch(cs -> cs != null && cs.getService() != null
            && cs.getService().getServiceName() != null
            && cs.getService().getServiceName().equalsIgnoreCase("Bảo vệ 24/7"));

        LocalDate start = contract.getStartDate() != null ? contract.getStartDate() : LocalDate.now();

        if (!hasElectricity) {
            RentalServiceItem electricity = rentalServiceRepository.findByServiceNameIgnoreCase("Điện")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình dịch vụ 'Điện'"));
            ContractService cs = ContractService.builder()
                .contract(contract)
                .service(electricity)
                .quantity(1)
                .startDate(start)
                .endDate(null)
                .previousReading(null)
                .currentReading(null)
                .build();
            contractServiceRepository.save(cs);
        }

        if (!hasWater) {
            RentalServiceItem water = rentalServiceRepository.findByServiceNameIgnoreCase("Nước")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình dịch vụ 'Nước'"));
            ContractService cs = ContractService.builder()
                .contract(contract)
                .service(water)
                .quantity(1)
                .startDate(start)
                .endDate(null)
                .previousReading(null)
                .currentReading(null)
                .build();
            contractServiceRepository.save(cs);
        }

        if (!hasSecurity) {
            RentalServiceItem security = rentalServiceRepository.findByServiceNameIgnoreCase("Bảo vệ 24/7")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình dịch vụ 'Bảo vệ 24/7'"));
            ContractService cs = ContractService.builder()
                .contract(contract)
                .service(security)
                .quantity(1)
                .startDate(start)
                .endDate(null)
                .previousReading(null)
                .currentReading(null)
                .build();
            contractServiceRepository.save(cs);
        }
        }

    @Override
    public ContractServiceResponse addServiceToContract(Long contractId, ContractServiceRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));
        assertTenantOwnsContract(contract);

        RentalServiceItem service = rentalServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        String serviceName = service.getServiceName() != null ? service.getServiceName().trim() : "";
        boolean isParking = "Giữ xe máy".equalsIgnoreCase(serviceName);

        // Lock quantity = 1 for all services except parking.
        if (!isParking) {
            request.setQuantity(1);
        } else if (request.getQuantity() == null || request.getQuantity() < 1) {
            request.setQuantity(1);
        }

        // Tenants cannot manually add utilities/fixed services.
        if (isTenant()) {
            String n = serviceName;
            if ("Điện".equalsIgnoreCase(n) || "Nước".equalsIgnoreCase(n)) {
                throw new RuntimeException("Điện/Nước là dịch vụ tự động, không thể đăng ký thủ công");
            }
            if (n.toLowerCase().contains("bảo vệ") || n.toLowerCase().contains("bao ve")) {
                throw new RuntimeException("Bảo vệ là dịch vụ cố định, hệ thống tự động áp dụng");
            }
        }

        ContractService entity = ContractServiceMapper.toEntity(request, service);
        entity.setContract(contract);

        return ContractServiceMapper.toResponse(contractServiceRepository.save(entity));
    }

    @Override
    public List<ContractServiceResponse> getServicesByContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new EntityNotFoundException("Contract not found"));
        assertTenantOwnsContract(contract);

        // Backfill fixed services for ACTIVE contracts (idempotent)
        ensureFixedServicesForActiveContract(contract);
        return contractServiceRepository.findByContractId(contractId).stream()
                .map(ContractServiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void removeService(Long id) {
        contractServiceRepository.deleteById(id);
    }

    @Override
    @Audited(action = AuditAction.REMOVE_SERVICE, targetType = "CONTRACT_SERVICE", description = "Hủy dịch vụ (hiệu lực cuối tháng)")
    public ContractServiceResponse cancelServiceEffectiveEndOfMonth(Long contractId, Long contractServiceId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));
        assertTenantOwnsContract(contract);

        ContractService cs = contractServiceRepository.findById(contractServiceId)
                .orElseThrow(() -> new EntityNotFoundException("Contract service not found"));
        if (cs.getContract() == null || !cs.getContract().getId().equals(contract.getId())) {
            throw new RuntimeException("Dịch vụ không thuộc hợp đồng");
        }

        // Fixed services cannot be cancelled.
        String n = cs.getService() != null && cs.getService().getServiceName() != null
                ? cs.getService().getServiceName().trim() : "";
        String nLower = n.toLowerCase();
        if ("Điện".equalsIgnoreCase(n) || "Nước".equalsIgnoreCase(n)) {
            throw new RuntimeException("Điện/Nước là dịch vụ tự động, không thể hủy");
        }
        if (nLower.contains("bảo vệ") || nLower.contains("bao ve")) {
            throw new RuntimeException("Bảo vệ là dịch vụ cố định, không thể hủy");
        }

        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        cs.setEndDate(endOfMonth);
        return ContractServiceMapper.toResponse(contractServiceRepository.save(cs));
    }

    @Override
    @Audited(action = AuditAction.MANUAL_ADJUSTMENT, targetType = "CONTRACT_SERVICE", description = "Cập nhật chỉ số điện/nước")
    public ContractServiceResponse updateMeterReadings(Long contractId, Long contractServiceId, MeterReadingUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Thiếu dữ liệu");
        }

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        ContractService cs = contractServiceRepository.findById(contractServiceId)
                .orElseThrow(() -> new EntityNotFoundException("Contract service not found"));
        if (cs.getContract() == null || !cs.getContract().getId().equals(contract.getId())) {
            throw new RuntimeException("Dịch vụ không thuộc hợp đồng");
        }

        if (request.getPreviousReading() != null) {
            cs.setPreviousReading(request.getPreviousReading());
        }
        if (request.getCurrentReading() != null) {
            cs.setCurrentReading(request.getCurrentReading());
        }
        return ContractServiceMapper.toResponse(contractServiceRepository.save(cs));
    }
}
