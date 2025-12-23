package com.example.rental.mapper;

import com.example.rental.dto.room.RoomResponse;
import com.example.rental.entity.Room;
import com.example.rental.dto.room.RoomImageResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoomMapper {

    public static RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomCode(room.getRoomCode())
                .branchCode(room.getBranchCode())
                .roomNumber(room.getRoomNumber())
                .area(room.getArea())
                .price(room.getPrice())
                .status(room.getStatus())
                .description(room.getDescription())
                .images(
                    room.getImages() != null
                            ? room.getImages().stream()
                            .map(img -> new RoomImageResponse(img.getId(), img.getImageUrl(), img.getIsThumbnail()))
                            .collect(Collectors.toList())
                            : null
                )
                .build();
    }
}