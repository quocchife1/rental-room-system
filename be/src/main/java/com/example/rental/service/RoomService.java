package com.example.rental.service;

import com.example.rental.dto.room.RoomRequest;
import com.example.rental.dto.room.RoomResponse;
import com.example.rental.entity.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomService {
    List<RoomResponse> getAllRooms();
    RoomResponse createRoom(RoomRequest request);
    RoomResponse updateRoom(Long id, RoomRequest request);
    RoomResponse updateRoomDescription(Long id, String description);
    RoomResponse updateRoomStatus(Long id, RoomStatus status);
    void deleteRoom(Long id);
    RoomResponse getRoomById(Long id);
    RoomResponse getRoomByCode(String roomCode);
    List<RoomResponse> getRoomsByBranchCode(String branchCode);
    List<RoomResponse> getRoomsByStatus(RoomStatus status);
    Page<RoomResponse> getRoomsByBranchCode(String branchCode, Pageable pageable);
    Page<RoomResponse> getRoomsByStatus(RoomStatus status, Pageable pageable);
}
