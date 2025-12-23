package com.example.rental.dto.room;

import com.example.rental.entity.RoomStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RoomResponse {
    private Long id;
    private String roomCode;
    private String branchCode;
    private String roomNumber;
    private BigDecimal area;
    private BigDecimal price;
    private RoomStatus status;
    private String description;
    private List<RoomImageResponse> images;
}