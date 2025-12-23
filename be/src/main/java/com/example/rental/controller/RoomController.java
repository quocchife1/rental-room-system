package com.example.rental.controller;

import com.example.rental.dto.room.RoomRequest;
import com.example.rental.dto.room.RoomResponse;
import com.example.rental.entity.RoomStatus;
import com.example.rental.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room API", description = "Quản lý phòng tại các chi nhánh")
@SecurityRequirement(name = "Bearer Authentication")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "Lấy tất cả phòng", description = "Truy xuất danh sách tất cả phòng trong hệ thống")
    @GetMapping
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<List<RoomResponse>>> getAllRooms() {
        List<RoomResponse> list = roomService.getAllRooms();
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Rooms fetched", list));
    }

    @Operation(summary = "Tạo phòng mới", description = "Thêm mới một phòng vào hệ thống với branchCode và roomNumber")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<RoomResponse>> createRoom(@RequestBody RoomRequest request) {
        RoomResponse resp = roomService.createRoom(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(com.example.rental.dto.ApiResponseDto.success(201, "Room created", resp));
    }

    @Operation(summary = "Cập nhật phòng theo ID", description = "Chỉnh sửa thông tin phòng dựa trên ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<RoomResponse>> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
        RoomResponse resp = roomService.updateRoom(id, request);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Room updated", resp));
    }

    @Operation(summary = "Cập nhật mô tả phòng", description = "Manager/Admin cập nhật mô tả phòng")
    @PutMapping("/{id}/description")
        @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<RoomResponse>> updateRoomDescription(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body
    ) {
        String description = body == null ? null : body.get("description");
        RoomResponse resp = roomService.updateRoomDescription(id, description);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Room description updated", resp));
    }

    @Operation(summary = "Cập nhật trạng thái phòng", description = "Đổi trạng thái phòng (AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE) mà không cần gửi toàn bộ RoomRequest")
    @PutMapping("/{id}/status")
        @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<RoomResponse>> updateRoomStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body
    ) {
        String statusStr = body == null ? null : body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest().body(com.example.rental.dto.ApiResponseDto.error(400, "Missing status", (RoomResponse) null));
        }
        try {
            RoomStatus status = RoomStatus.valueOf(statusStr.toUpperCase());
            RoomResponse resp = roomService.updateRoomStatus(id, status);
            return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Room status updated", resp));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(com.example.rental.dto.ApiResponseDto.error(400, "Invalid status", (RoomResponse) null));
        }
    }

    @Operation(summary = "Xóa phòng theo ID", description = "Xóa vĩnh viễn phòng trong hệ thống")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Room deleted"));
    }

    @Operation(summary = "Lấy phòng theo ID", description = "Truy xuất thông tin phòng bằng ID")
    @GetMapping("/{id}")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<RoomResponse>> getRoomById(@PathVariable Long id) {
        RoomResponse resp = roomService.getRoomById(id);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Room fetched", resp));
    }

    @Operation(summary = "Lấy phòng theo roomCode", description = "Truy xuất phòng bằng mã roomCode (VD: CN01101)")
    @GetMapping("/code/{roomCode}")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<RoomResponse>> getRoomByCode(@PathVariable String roomCode) {
        RoomResponse resp = roomService.getRoomByCode(roomCode);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Room fetched", resp));
    }

    @Operation(summary = "Lấy danh sách phòng theo branchCode", description = "Lấy tất cả phòng thuộc một chi nhánh thông qua branchCode (VD: CN01)")
    @GetMapping("/branch/{branchCode}")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<RoomResponse>>> getRoomsByBranchCode(@PathVariable String branchCode) {
        java.util.List<RoomResponse> list = roomService.getRoomsByBranchCode(branchCode);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Rooms fetched", list));
    }

    @GetMapping("/branch/{branchCode}/paged")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<org.springframework.data.domain.Page<RoomResponse>>> getRoomsByBranchCodePaged(@PathVariable String branchCode, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<RoomResponse> page = roomService.getRoomsByBranchCode(branchCode, pageable);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Rooms page fetched", page));
    }

    @Operation(summary = "Lấy phòng theo trạng thái", description = "Truy xuất danh sách phòng theo trạng thái (AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE)")
    @GetMapping("/status/{status}")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<java.util.List<RoomResponse>>> getRoomsByStatus(@PathVariable RoomStatus status) {
        java.util.List<RoomResponse> list = roomService.getRoomsByStatus(status);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Rooms fetched", list));
    }

    @GetMapping("/status/{status}/paged")
    public ResponseEntity<com.example.rental.dto.ApiResponseDto<org.springframework.data.domain.Page<RoomResponse>>> getRoomsByStatusPaged(@PathVariable RoomStatus status, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<RoomResponse> page = roomService.getRoomsByStatus(status, pageable);
        return ResponseEntity.ok(com.example.rental.dto.ApiResponseDto.success(200, "Rooms page fetched", page));
    }
}
