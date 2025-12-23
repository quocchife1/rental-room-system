package com.example.rental.dto.room;

import com.example.rental.entity.RoomStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomSearchRequest {
    private String branchCode;
    private String roomNumber;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minArea;
    private BigDecimal maxArea;
    private RoomStatus status;
}