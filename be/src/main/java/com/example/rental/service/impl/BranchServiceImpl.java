package com.example.rental.service.impl;

import com.example.rental.dto.branch.BranchRequest;
import com.example.rental.dto.branch.BranchResponse;
import com.example.rental.entity.Branch;
import com.example.rental.entity.Contract;
import com.example.rental.entity.Room;
import com.example.rental.exception.ResourceNotFoundException;
import com.example.rental.mapper.BranchMapper;
import com.example.rental.repository.BranchRepository;
import com.example.rental.repository.ContractRepository;
import com.example.rental.repository.EmployeeRepository;
import com.example.rental.repository.InvoiceRepository;
import com.example.rental.repository.MaintenanceRequestRepository;
import com.example.rental.repository.ReservationRepository;
import com.example.rental.repository.RoomRepository;
import com.example.rental.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final EmployeeRepository employeeRepository;

    // ===== ENTITY LAYER =====
    @Override
    public Optional<Branch> findById(Long id) {
        return branchRepository.findById(id);
    }


    @Override
    public Optional<Branch> findByBranchCode(String branchCode) {
        return branchRepository.findByBranchCode(branchCode);
    }

    // ===== DTO LAYER =====
    @Override
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(branchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        return branchMapper.toResponse(branch);
    }

    @Override
    public BranchResponse getBranchByCode(String branchCode) {
        Branch branch = branchRepository.findByBranchCode(branchCode)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "branchCode", branchCode));
        return branchMapper.toResponse(branch);
    }

    @Override
    public BranchResponse createBranch(BranchRequest request) {
        // B1: Chuyển từ DTO sang entity
        Branch branch = branchMapper.toEntity(request);

        // B2: Lưu trước để có ID
        Branch saved = branchRepository.save(branch);

        // B3: Sinh mã branchCode theo ID
        String code = String.format("CN%02d", saved.getId());
        saved.setBranchCode(code);

        // B4: Lưu lại
        saved = branchRepository.save(saved);

        // B5: Trả về DTO
        return branchMapper.toResponse(saved);
    }

    @Override
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));

        // Cập nhật các field khác
        branchMapper.updateEntityFromRequest(request, branch);

        // Không cho chỉnh lại branchCode từ request (vì tự động sinh)
        // Nếu bạn muốn cho người dùng sửa, bỏ đoạn này

        Branch updated = branchRepository.save(branch);
        return branchMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));

        String branchCode = branch.getBranchCode();

        // Unassign employees that are pointing to this branch (FK via branch_code)
        if (branchCode != null && !branchCode.isBlank()) {
            employeeRepository.unassignBranchByBranchCode(branchCode);
        }

        // Delete rooms and all related data
        java.util.List<Room> rooms = (branchCode == null || branchCode.isBlank())
                ? java.util.List.of()
                : roomRepository.findByBranchCode(branchCode);

        if (rooms != null && !rooms.isEmpty()) {
            java.util.List<Long> roomIds = rooms.stream()
                    .map(Room::getId)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            if (!roomIds.isEmpty()) {
                // Invoices -> Contracts -> Rooms
                java.util.List<Contract> contracts = contractRepository.findByRoom_IdIn(roomIds);
                if (contracts != null && !contracts.isEmpty()) {
                    java.util.List<Long> contractIds = contracts.stream()
                            .map(Contract::getId)
                            .filter(java.util.Objects::nonNull)
                            .toList();
                    if (!contractIds.isEmpty()) {
                        invoiceRepository.deleteByContract_IdIn(contractIds);
                    }
                    contractRepository.deleteAll(contracts);
                }

                // Maintenance requests (cascades its images)
                maintenanceRequestRepository.deleteByRoom_IdIn(roomIds);

                // Reservations
                reservationRepository.deleteByRoom_IdIn(roomIds);
            }

            // Rooms (cascades room images)
            roomRepository.deleteAll(rooms);
        }

        branchRepository.delete(branch);
    }
}
