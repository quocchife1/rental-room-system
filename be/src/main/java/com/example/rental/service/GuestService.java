package com.example.rental.service;

import com.example.rental.entity.Guest;
import java.util.Optional;

public interface GuestService {
    // Đăng ký khách mới
    Guest registerNewGuest(Guest guest);

    // Lấy thông tin khách theo ID
    Optional<Guest> findById(Long id);

    // Lấy thông tin khách theo Username
    Optional<Guest> findByUsername(String username);
    
    // Cập nhật hồ sơ
    Guest updateGuestProfile(Guest guest);
}