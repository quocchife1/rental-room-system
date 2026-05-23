package com.example.rental.service.impl;

import com.example.rental.dto.branch.BranchRequest;
import com.example.rental.entity.Branch;
import com.example.rental.entity.Contract;
import com.example.rental.entity.Room;
import com.example.rental.exception.ResourceNotFoundException;
import com.example.rental.mapper.BranchMapper;
import com.example.rental.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BranchServiceImpl Tests")
class BranchServiceImplTest {

    @Mock private BranchRepository branchRepository;
    @Mock private BranchMapper branchMapper;
    @Mock private RoomRepository roomRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private MaintenanceRequestRepository maintenanceRequestRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private BranchServiceImpl branchService;

    @Test
    void findById_ShouldReturnBranch() {
        Branch branch = new Branch();
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        assertThat(branchService.findById(1L)).isPresent();
    }

    @Test
    void getAllBranches_ShouldReturnList() {
        when(branchRepository.findAll()).thenReturn(List.of(new Branch()));
        when(branchMapper.toResponse(any())).thenReturn(com.example.rental.dto.branch.BranchResponse.builder().build());
        assertThat(branchService.getAllBranches()).hasSize(1);
    }

    @Test
    void createBranch_ShouldSaveAndFormatCode() {
        BranchRequest req = new BranchRequest();
        Branch branch = new Branch();
        
        when(branchMapper.toEntity(req)).thenReturn(branch);
        when(branchRepository.save(branch)).thenReturn(branch); // Save lần 1 (để lấy ID)
        
        // Mock giả lập ID = 5
        branch.setId(5L);
        when(branchRepository.save(any(Branch.class))).thenReturn(branch); // Save lần 2
        
        branchService.createBranch(req);
        
        verify(branchRepository, times(2)).save(branch);
        assertThat(branch.getBranchCode()).isEqualTo("CN05");
    }

    @Test
    void deleteBranch_ShouldCascadeDeleteEverything() {
        // Mock branch tồn tại
        Branch branch = new Branch();
        branch.setBranchCode("CN01");
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        
        // Mock danh sách phòng
        Room room = new Room(); room.setId(10L);
        when(roomRepository.findByBranchCode("CN01")).thenReturn(List.of(room));
        
        // Mock hợp đồng
        Contract contract = new Contract(); contract.setId(20L);
        when(contractRepository.findByRoom_IdIn(List.of(10L))).thenReturn(List.of(contract));

        // Thực hiện xóa
        branchService.deleteBranch(1L);

        // Verify các bước xóa theo thứ tự
        verify(employeeRepository).unassignBranchByBranchCode("CN01");
        verify(invoiceRepository).deleteByContract_IdIn(List.of(20L));
        verify(contractRepository).deleteAll(anyList());
        verify(maintenanceRequestRepository).deleteByRoom_IdIn(List.of(10L));
        verify(reservationRepository).deleteByRoom_IdIn(List.of(10L));
        verify(roomRepository).deleteAll(anyList());
        verify(branchRepository).delete(branch);
    }

    @Test
    void getBranchById_NotFound_ThrowsException() {
        when(branchRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> branchService.getBranchById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateBranch_Success() {
        Branch branch = new Branch();
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(branchRepository.save(any())).thenReturn(branch);
        
        branchService.updateBranch(1L, new BranchRequest());
        
        verify(branchMapper).updateEntityFromRequest(any(), eq(branch));
        verify(branchRepository).save(branch);
    }
}
