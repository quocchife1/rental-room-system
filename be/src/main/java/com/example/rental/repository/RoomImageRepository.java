package com.example.rental.repository;

import com.example.rental.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    // Tìm ảnh theo ID phòng
    List<RoomImage> findByRoomId(Long roomId);

    // Tìm ảnh thumbnail của phòng
    RoomImage findByRoomIdAndIsThumbnailTrue(Long roomId);

    Optional<RoomImage> findByIdAndRoomId(Long id, Long roomId);
}