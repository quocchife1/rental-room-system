package com.example.rental.dto.room;

import com.example.rental.entity.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomRequest {

    @Schema(example = "CN01", description = "Mã chi nhánh muốn tạo phòng")
    private String branchCode;

    @Schema(example = "101", description = "Số phòng trong chi nhánh")
    private String roomNumber;

    @Schema(example = "25.5", description = "Diện tích phòng (m²)")
    private BigDecimal area;

    @Schema(example = "3500000", description = "Giá phòng mỗi tháng (VNĐ)")
    private BigDecimal price;

    @Schema(example = "AVAILABLE", description = "Trạng thái phòng: AVAILABLE, RENTED, MAINTENANCE")
    private RoomStatus status;

    @Schema(example = "Có máy lạnh, view đẹp", description = "Mô tả chi tiết phòng")
    private String description;
}
