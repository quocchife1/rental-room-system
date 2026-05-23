package com.example.rental.service.impl;

import com.example.rental.dto.room.RoomRequest;
import com.example.rental.dto.room.RoomResponse;
import com.example.rental.entity.*;
import com.example.rental.mapper.RoomMapper;
import com.example.rental.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoomServiceImpl Tests")
class RoomServiceImplTest {

    @Mock private RoomRepository roomRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private void mockAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
    }

    @Test
    @DisplayName("Create Room - Success for Admin")
    void createRoom_Success() {
        mockAdmin();
        RoomRequest request = new RoomRequest();
        request.setBranchCode("B01");
        request.setRoomNumber("101");

        Branch branch = new Branch();
        branch.setBranchCode("B01");

        when(branchRepository.findByBranchCode("B01")).thenReturn(Optional.of(branch));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<RoomMapper> mapper = mockStatic(RoomMapper.class)) {
            mapper.when(() -> RoomMapper.toResponse(any())).thenReturn(RoomResponse.builder().build());
            
            RoomResponse response = roomService.createRoom(request);
            
            assertThat(response).isNotNull();
            verify(roomRepository).save(any(Room.class));
        }
    }

    @Test
    @DisplayName("Create Room - Fail for non-Admin")
    void createRoom_FailAccessDenied() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        assertThatThrownBy(() -> roomService.createRoom(new RoomRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Bạn không có quyền");
    }

    @Test
    @DisplayName("Update Room Status - Success")
    void updateRoomStatus_Success() {
        mockAdmin();
        Room room = new Room();
        room.setId(1L);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<RoomMapper> mapper = mockStatic(RoomMapper.class)) {
            mapper.when(() -> RoomMapper.toResponse(any())).thenReturn(RoomResponse.builder().build());
            
            roomService.updateRoomStatus(1L, RoomStatus.AVAILABLE);
            
            assertThat(room.getStatus()).isEqualTo(RoomStatus.AVAILABLE);
        }
    }

// Sửa hàm updateRoomStatus_ManagerFailReserved
    @Test
    @DisplayName("Update Room Status - Manager cannot change Reserved room")
    void updateRoomStatus_ManagerFailReserved() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("manager", "pass", List.of(new SimpleGrantedAuthority("ROLE_MANAGER")))
        );
        
        // Khởi tạo đầy đủ Branch và Room
        Branch branch = Branch.builder().branchCode("B01").build();
        Room room = Room.builder().id(1L).status(RoomStatus.RESERVED).branch(branch).build();
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        
        Employees emp = Employees.builder().branch(branch).build();
        when(employeeRepository.findByUsername("manager")).thenReturn(Optional.of(emp));
    
        assertThatThrownBy(() -> roomService.updateRoomStatus(1L, RoomStatus.AVAILABLE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Không thể thay đổi trạng thái khi phòng đang thuê hoặc đã đặt");
    }
}
