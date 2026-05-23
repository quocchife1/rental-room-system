package com.example.rental.service.impl;

import com.example.rental.entity.Guest;
import com.example.rental.entity.UserStatus;
import com.example.rental.repository.GuestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GuestServiceImpl Tests")
class GuestServiceImplTest {

    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private GuestServiceImpl guestService;

    @Test
    @DisplayName("Register New Guest - Success")
    void registerNewGuest_Success() {
        Guest guest = new Guest();
        guest.setUsername("guest01");
        guest.setEmail("guest@test.com");

        when(guestRepository.existsByUsername("guest01")).thenReturn(false);
        when(guestRepository.existsByEmail("guest@test.com")).thenReturn(false);
        when(guestRepository.save(any(Guest.class))).thenAnswer(i -> i.getArgument(0));

        Guest result = guestService.registerNewGuest(guest);

        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(guestRepository).save(guest);
    }

    @Test
    @DisplayName("Register New Guest - Fail on Duplicate Username")
    void registerNewGuest_FailDuplicateUsername() {
        Guest guest = new Guest();
        guest.setUsername("exists");
        when(guestRepository.existsByUsername("exists")).thenReturn(true);

        assertThatThrownBy(() -> guestService.registerNewGuest(guest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Tên đăng nhập đã được sử dụng.");
    }

    @Test
    @DisplayName("Register New Guest - Fail on Duplicate Email")
    void registerNewGuest_FailDuplicateEmail() {
        Guest guest = new Guest();
        guest.setUsername("new");
        guest.setEmail("exists@test.com");
        when(guestRepository.existsByUsername("new")).thenReturn(false);
        when(guestRepository.existsByEmail("exists@test.com")).thenReturn(true);

        assertThatThrownBy(() -> guestService.registerNewGuest(guest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email đã được sử dụng.");
    }

    @Test
    @DisplayName("Update Guest Profile - Success")
    void updateGuestProfile_Success() {
        Guest existingGuest = new Guest();
        existingGuest.setId(1L);
        existingGuest.setFullName("Old Name");

        Guest updatedGuest = new Guest();
        updatedGuest.setId(1L);
        updatedGuest.setFullName("New Name");
        updatedGuest.setPhoneNumber("0909090909");

        when(guestRepository.findById(1L)).thenReturn(Optional.of(existingGuest));
        when(guestRepository.save(any(Guest.class))).thenAnswer(i -> i.getArgument(0));

        Guest result = guestService.updateGuestProfile(updatedGuest);

        assertThat(result.getFullName()).isEqualTo("New Name");
        assertThat(result.getPhoneNumber()).isEqualTo("0909090909");
        verify(guestRepository).save(existingGuest);
    }

    @Test
    @DisplayName("Update Guest Profile - Not Found")
    void updateGuestProfile_NotFound() {
        Guest guest = new Guest();
        guest.setId(99L);
        when(guestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guestService.updateGuestProfile(guest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Không tìm thấy khách hàng.");
    }
}
