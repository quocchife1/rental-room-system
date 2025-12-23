package com.example.rental.service.impl;

import com.example.rental.entity.Guest;
import com.example.rental.entity.UserStatus;
import com.example.rental.repository.GuestRepository;
import com.example.rental.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;

    @Override
    @Transactional
    public Guest registerNewGuest(Guest guest) {
        if (guestRepository.existsByUsername(guest.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã được sử dụng.");
        }
        if (guestRepository.existsByEmail(guest.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng.");
        }

        // Logic nghiệp vụ: Mã hóa mật khẩu, đặt trạng thái ACTIVE
        guest.setStatus(UserStatus.ACTIVE);
        
        return guestRepository.save(guest);
    }

    @Override
    public Optional<Guest> findById(Long id) {
        return guestRepository.findById(id);
    }

    @Override
    public Optional<Guest> findByUsername(String username) {
        return guestRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public Guest updateGuestProfile(Guest updatedGuest) {
        Guest existingGuest = guestRepository.findById(updatedGuest.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng."));

        // Cập nhật thông tin
        existingGuest.setFullName(updatedGuest.getFullName());
        existingGuest.setPhoneNumber(updatedGuest.getPhoneNumber());
        existingGuest.setEmail(updatedGuest.getEmail());

        return guestRepository.save(existingGuest);
    }
}