package com.example.rental.service.impl;

import com.example.rental.dto.room.RoomRequest;
import com.example.rental.dto.room.RoomResponse;
import com.example.rental.entity.Branch;
import com.example.rental.entity.Room;
import com.example.rental.entity.RoomStatus;
import com.example.rental.mapper.RoomMapper;
import com.example.rental.repository.BranchRepository;
import com.example.rental.repository.EmployeeRepository;
import com.example.rental.repository.RoomRepository;
import com.example.rental.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.example.rental.security.Audited;
import com.example.rental.entity.AuditAction;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;

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

        var emp = employeeRepository.findByUsername(username)
                .or(() -> employeeRepository.findByUsernameIgnoreCase(username))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên."));

        if (emp.getBranch() == null || emp.getBranch().getBranchCode() == null) {
            throw new RuntimeException("Nhân viên chưa được gán chi nhánh.");
        }

        return emp.getBranch().getBranchCode();
    }

    private void enforceManagerSameBranch(Room room) {
        if (room == null) return;
        // ADMIN/DIRECTOR: không giới hạn chi nhánh
        if (hasRole("ADMIN") || hasRole("DIRECTOR")) return;

        // MANAGER: chỉ thao tác phòng thuộc chi nhánh của mình
        if (hasRole("MANAGER")) {
            String myBranch = getCurrentEmployeeBranchCode();
            String roomBranch = room.getBranch() != null ? room.getBranch().getBranchCode() : room.getBranchCode();
            if (roomBranch == null || !roomBranch.equalsIgnoreCase(myBranch)) {
                throw new RuntimeException("Bạn chỉ có thể thao tác phòng thuộc chi nhánh của mình: " + myBranch);
            }
        }
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        // MANAGER: chỉ xem chi nhánh của mình
        if (hasRole("MANAGER") && !hasRole("ADMIN")) {
            String branchCode = getCurrentEmployeeBranchCode();
            return roomRepository.findByBranchCode(branchCode).stream()
                .map(RoomMapper::toResponse)
                .collect(Collectors.toList());
        }

        return roomRepository.findAll().stream()
            .map(RoomMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Audited(action = AuditAction.CREATE_ROOM, targetType = "ROOM", description = "Tạo phòng mới")
    public RoomResponse createRoom(RoomRequest request) {
        if (!hasRole("ADMIN") && !hasRole("DIRECTOR")) {
            throw new RuntimeException("Bạn không có quyền tạo phòng.");
        }
        Branch branch = branchRepository.findByBranchCode(request.getBranchCode())
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        String roomCode = branch.getBranchCode() + request.getRoomNumber();

        Room room = Room.builder()
                .roomCode(roomCode)
                .branch(branch)
                .branchCode(branch.getBranchCode())
                .roomNumber(request.getRoomNumber())
                .area(request.getArea())
                .price(request.getPrice())
                .status(request.getStatus())
                .description(request.getDescription())
                .build();

        return RoomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Audited(action = AuditAction.UPDATE_ROOM, targetType = "ROOM", description = "Cập nhật phòng")
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        if (!hasRole("ADMIN") && !hasRole("DIRECTOR")) {
            throw new RuntimeException("Bạn không có quyền cập nhật toàn bộ thông tin phòng.");
        }
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        Branch branch = branchRepository.findByBranchCode(request.getBranchCode())
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        String roomCode = branch.getBranchCode() + request.getRoomNumber();

        room.setRoomCode(roomCode);
        room.setBranch(branch);
        room.setBranchCode(branch.getBranchCode());
        room.setRoomNumber(request.getRoomNumber());
        room.setArea(request.getArea());
        room.setPrice(request.getPrice());
        room.setStatus(request.getStatus());
        room.setDescription(request.getDescription());

        return RoomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Audited(action = AuditAction.UPDATE_ROOM, targetType = "ROOM", description = "Cập nhật mô tả phòng")
    public RoomResponse updateRoomDescription(Long id, String description) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        enforceManagerSameBranch(room);

        if (!hasRole("ADMIN") && !hasRole("DIRECTOR") && !hasRole("MANAGER")) {
            throw new RuntimeException("Bạn không có quyền cập nhật mô tả phòng.");
        }

        room.setDescription(description);
        return RoomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Audited(action = AuditAction.CHANGE_ROOM_STATUS, targetType = "ROOM", description = "Đổi trạng thái phòng")
    public RoomResponse updateRoomStatus(Long id, RoomStatus status) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        enforceManagerSameBranch(room);

        // MANAGER: không được đổi trạng thái phòng đang thuê/đã đặt
        if (hasRole("MANAGER") && !hasRole("ADMIN")) {
            if (room.getStatus() == com.example.rental.entity.RoomStatus.OCCUPIED
                    || room.getStatus() == com.example.rental.entity.RoomStatus.RESERVED) {
                throw new IllegalStateException("Không thể thay đổi trạng thái khi phòng đang thuê hoặc đã đặt.");
            }
        }

        room.setStatus(status);
        return RoomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Audited(action = AuditAction.DELETE_ROOM, targetType = "ROOM", description = "Xoá phòng")
    public void deleteRoom(Long id) {
        if (!hasRole("ADMIN") && !hasRole("DIRECTOR")) {
            throw new RuntimeException("Bạn không có quyền xoá phòng.");
        }
        roomRepository.deleteById(id);
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        enforceManagerSameBranch(room);
        return RoomMapper.toResponse(room);
    }

    @Override
    public RoomResponse getRoomByCode(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode);
        if (room == null) throw new EntityNotFoundException("Room not found");
        enforceManagerSameBranch(room);
        return RoomMapper.toResponse(room);
    }

    @Override
    public List<RoomResponse> getRoomsByBranchCode(String branchCode) {
        // MANAGER: chỉ xem chi nhánh của mình
        if (hasRole("MANAGER") && !hasRole("ADMIN")) {
            String myBranch = getCurrentEmployeeBranchCode();
            if (branchCode != null && !branchCode.trim().isEmpty() && !myBranch.equalsIgnoreCase(branchCode.trim())) {
                throw new RuntimeException("Bạn chỉ có thể xem phòng thuộc chi nhánh của mình: " + myBranch);
            }
            branchCode = myBranch;
        }
        return roomRepository.findByBranchCode(branchCode).stream()
                .map(RoomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RoomResponse> getRoomsByBranchCode(String branchCode, Pageable pageable) {
        if (hasRole("MANAGER") && !hasRole("ADMIN")) {
            String myBranch = getCurrentEmployeeBranchCode();
            if (branchCode != null && !branchCode.trim().isEmpty() && !myBranch.equalsIgnoreCase(branchCode.trim())) {
                throw new RuntimeException("Bạn chỉ có thể xem phòng thuộc chi nhánh của mình: " + myBranch);
            }
            branchCode = myBranch;
        }
        Page<Room> page = roomRepository.findByBranchCode(branchCode, pageable);
        List<RoomResponse> content = page.getContent().stream().map(RoomMapper::toResponse).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public List<RoomResponse> getRoomsByStatus(RoomStatus status) {
        return roomRepository.findByStatus(status).stream()
                .map(RoomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RoomResponse> getRoomsByStatus(RoomStatus status, Pageable pageable) {
        Page<Room> page = roomRepository.findByStatus(status, pageable);
        List<RoomResponse> content = page.getContent().stream().map(RoomMapper::toResponse).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }
}
