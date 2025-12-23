package com.example.rental.service;

import com.example.rental.dto.reservation.ReservationRequest;
import com.example.rental.dto.reservation.ReservationResponse;
import com.example.rental.dto.contract.ContractPrefillResponse;
import com.example.rental.entity.Reservation;
import com.example.rental.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ReservationService {
    // Tạo phiếu đặt phòng mới (Đầu tiên sẽ là PENDING)
    Reservation createReservation(Reservation reservation);
    
    // Tạo phiếu đặt phòng từ request
    ReservationResponse createReservation(ReservationRequest request);

    // Lấy phiếu đặt phòng theo ID
    Optional<Reservation> findById(Long id);
    
    // Lấy chi tiết phiếu đặt phòng theo ID (trả về response)
    ReservationResponse getReservationById(Long id);

    // Lấy danh sách phiếu theo ID người thuê
    List<Reservation> findReservationsByTenantId(Long tenantId);
    
    // Lấy danh sách phiếu theo ID người thuê (trả về page response)
    Page<ReservationResponse> getReservationsByTenant(Long tenantId, Pageable pageable);

    // Lấy danh sách giữ phòng của người dùng hiện tại (dựa trên JWT)
    Page<ReservationResponse> getMyReservations(Pageable pageable);

    // Lấy danh sách phiếu theo trạng thái
    List<Reservation> findReservationsByStatus(ReservationStatus status);
    
    // Lấy danh sách phiếu theo trạng thái (trả về page response)
    Page<ReservationResponse> getReservationsByStatus(String status, Pageable pageable);
    
    // Lấy danh sách phiếu theo phòng
    List<ReservationResponse> getReservationsByRoom(Long roomId);

    // Xác nhận giữ phòng (Chuyển từ PENDING sang HOLD) - trả về response
    ReservationResponse confirmReservation(Long reservationId);
    
    // Hủy phiếu đặt phòng (Chuyển sang CANCELLED)
    Reservation cancelReservation(Long reservationId);

    // Lễ tân xử lý sau khi khách tham quan
    ReservationResponse markCompleted(Long reservationId);

    // Lễ tân xử lý trường hợp khách không đến
    ReservationResponse markNoShow(Long reservationId);

    // Tra cứu phiếu đặt theo mã/khách/phòng
    Page<ReservationResponse> searchReservations(String query, Pageable pageable);

    // Lễ tân xem tất cả phiếu theo chi nhánh mình (lọc status và/hoặc query nếu cần)
    Page<ReservationResponse> getMyBranchReservations(String status, String query, Pageable pageable);
    
    // Chuyển phiếu đặt phòng thành hợp đồng
    Long convertReservationToContract(Long reservationId);

    // Lấy dữ liệu prefill để lập hợp đồng từ phiếu giữ phòng
    ContractPrefillResponse getContractPrefill(Long reservationId);

    // Đánh dấu phiếu giữ phòng đã được lập hợp đồng (không giải phóng phòng)
    ReservationResponse markContracted(Long reservationId);
}