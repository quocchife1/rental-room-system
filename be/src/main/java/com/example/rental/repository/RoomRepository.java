package com.example.rental.repository;

import com.example.rental.entity.Room;
import com.example.rental.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Room findByRoomCode(String roomCode);
    List<Room> findByBranchCode(String branchCode);
    List<Room> findByStatus(RoomStatus status);
    org.springframework.data.domain.Page<Room> findByBranchCode(String branchCode, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Room> findByStatus(RoomStatus status, org.springframework.data.domain.Pageable pageable);

    Optional<Room> findByBranchCodeAndRoomNumber(String branchCode, String roomNumber);
}
