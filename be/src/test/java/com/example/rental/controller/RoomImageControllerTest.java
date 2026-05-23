package com.example.rental.controller;

import com.example.rental.entity.Room;
import com.example.rental.entity.RoomImage;
import com.example.rental.repository.EmployeeRepository;
import com.example.rental.repository.RoomImageRepository;
import com.example.rental.repository.RoomRepository;
import com.example.rental.utils.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@DisplayName("RoomImageController tests")
class RoomImageControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private RoomRepository roomRepository;

    @MockitoBean
    private RoomImageRepository roomImageRepository;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadRoomImages_shouldReturn201() throws Exception {
        Room room = new Room();
        room.setId(10L);

        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(fileStorageService.storeFile(any(), eq("rooms"))).thenReturn("img.jpg");
        when(roomImageRepository.findByRoomIdAndIsThumbnailTrue(10L)).thenReturn(null);

        RoomImage saved = RoomImage.builder().id(2L).imageUrl("/uploads/rooms/img.jpg").isThumbnail(true).room(room).build();
        when(roomImageRepository.save(any())).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile("images", "photo.jpg", "image/jpeg", "x".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/rooms/10/images")
                        .file(file)
                        .with(csrf()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists());
    }
}
