package com.example.rental.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomOccupancyDTO {
    private Long branchId;
    private String branchName;
    /**
     * Occupancy ratio in range [0..1].
     */
    private Double occupancyRate;
    private Integer totalRooms;
    private Integer occupiedRooms;
}
