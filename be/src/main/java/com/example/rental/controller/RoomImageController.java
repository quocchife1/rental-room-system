package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.dto.room.RoomImageResponse;
import com.example.rental.entity.Room;
import com.example.rental.entity.RoomImage;
import com.example.rental.repository.EmployeeRepository;
import com.example.rental.repository.RoomImageRepository;
import com.example.rental.repository.RoomRepository;
import com.example.rental.utils.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomImageController {

    private final FileStorageService fileStorageService;
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final EmployeeRepository employeeRepository;

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        String wanted1 = "ROLE_" + role;
        String wanted2 = role;
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (a == null || a.getAuthority() == null) continue;
            String v = a.getAuthority();
            if (wanted1.equalsIgnoreCase(v) || wanted2.equalsIgnoreCase(v)) return true;
        }
        return false;
    }

    private String getCurrentEmployeeBranchCode() {
        String username = getCurrentUsername();
        if (username == null) throw new UsernameNotFoundException("Unauthenticated");

        var emp = employeeRepository.findByUsername(username)
                .or(() -> employeeRepository.findByUsernameIgnoreCase(username))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên."));

        if (emp.getBranch() == null || emp.getBranch().getBranchCode() == null) {
            throw new RuntimeException("Nhân viên chưa được gán chi nhánh.");
        }

        return emp.getBranch().getBranchCode();
    }

    private void enforceManagerSameBranch(Room room) {
        if (room == null) return;
        if (hasRole("ADMIN")) return;
        if (hasRole("MANAGER")) {
            String myBranch = getCurrentEmployeeBranchCode();
            String roomBranch = room.getBranch() != null ? room.getBranch().getBranchCode() : room.getBranchCode();
            if (roomBranch == null || !roomBranch.equalsIgnoreCase(myBranch)) {
                throw new RuntimeException("Bạn chỉ có thể thao tác phòng thuộc chi nhánh của mình: " + myBranch);
            }
        }
    }

    @PostMapping(value = "/{roomId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER')")
    public ResponseEntity<ApiResponseDto<List<RoomImageResponse>>> uploadRoomImages(
            @PathVariable Long roomId,
            @RequestPart("images") MultipartFile[] images) throws IOException {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        enforceManagerSameBranch(room);

        // Nếu phòng chưa có thumbnail, set ảnh đầu tiên upload làm thumbnail
        boolean hasExistingThumbnail = roomImageRepository.findByRoomIdAndIsThumbnailTrue(roomId) != null;

        List<RoomImageResponse> saved = new ArrayList<>();

        for (int i = 0; i < images.length; i++) {
            MultipartFile f = images[i];
            // Lưu file vật lý
            String filename = fileStorageService.storeFile(f, "rooms");
            // Tạo đường dẫn URL (cần đảm bảo khớp với cấu hình ResourceHandler)
            String url = "/uploads/rooms/" + filename;

            RoomImage img = RoomImage.builder()
                    .room(room)
                    .imageUrl(url)
                .isThumbnail(!hasExistingThumbnail && i == 0)
                    .build();

            RoomImage persisted = roomImageRepository.save(img);
            saved.add(new RoomImageResponse(persisted.getId(), persisted.getImageUrl(), persisted.getIsThumbnail()));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(), "Uploaded room images", saved));
    }

    @GetMapping("/{roomId}/images")
    public ResponseEntity<ApiResponseDto<List<RoomImageResponse>>> listRoomImages(@PathVariable Long roomId) {
        List<RoomImage> imgs = roomImageRepository.findByRoomId(roomId);
        List<RoomImageResponse> resp = new ArrayList<>();
        
        for (RoomImage img : imgs) {
            resp.add(new RoomImageResponse(img.getId(), img.getImageUrl(), img.getIsThumbnail()));
        }
        
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "Room images", resp));
    }

    @PutMapping("/{roomId}/images/{imageId}/thumbnail")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER')")
    @Transactional
    public ResponseEntity<ApiResponseDto<List<RoomImageResponse>>> setThumbnail(
            @PathVariable Long roomId,
            @PathVariable Long imageId
    ) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        enforceManagerSameBranch(room);

        List<RoomImage> imgs = roomImageRepository.findByRoomId(roomId);
        if (imgs == null || imgs.isEmpty()) {
            throw new IllegalArgumentException("Room has no images");
        }

        boolean found = false;
        for (RoomImage img : imgs) {
            boolean isTarget = img != null && img.getId() != null && img.getId().equals(imageId);
            if (isTarget) found = true;
            if (img != null) {
                img.setIsThumbnail(isTarget);
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Image not found in this room");
        }

        roomImageRepository.saveAll(imgs);

        List<RoomImageResponse> resp = new ArrayList<>();
        for (RoomImage img : imgs) {
            resp.add(new RoomImageResponse(img.getId(), img.getImageUrl(), img.getIsThumbnail()));
        }
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "Thumbnail updated", resp));
    }

    @DeleteMapping("/{roomId}/images/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','MANAGER')")
    @Transactional
    public ResponseEntity<ApiResponseDto<List<RoomImageResponse>>> deleteRoomImage(
            @PathVariable Long roomId,
            @PathVariable Long imageId
    ) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        enforceManagerSameBranch(room);

        RoomImage img = roomImageRepository.findByIdAndRoomId(imageId, roomId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found in this room"));

        boolean wasThumbnail = Boolean.TRUE.equals(img.getIsThumbnail());
        String imageUrl = img.getImageUrl();

        roomImageRepository.delete(img);

        // Delete physical file (best-effort)
        if (imageUrl != null) {
            String prefix = "/uploads/rooms/";
            String filename = null;
            if (imageUrl.startsWith(prefix)) {
                filename = imageUrl.substring(prefix.length());
            } else if (imageUrl.startsWith("uploads/rooms/")) {
                filename = imageUrl.substring("uploads/rooms/".length());
            }
            if (filename != null && !filename.isBlank()) {
                fileStorageService.deleteFile("rooms", filename);
            }
        }

        // If deleted thumbnail, choose a new one if images remain
        List<RoomImage> remaining = roomImageRepository.findByRoomId(roomId);
        if (wasThumbnail && remaining != null && !remaining.isEmpty()) {
            boolean hasThumb = false;
            for (RoomImage r : remaining) {
                if (Boolean.TRUE.equals(r.getIsThumbnail())) {
                    hasThumb = true;
                    break;
                }
            }
            if (!hasThumb) {
                remaining.get(0).setIsThumbnail(true);
                roomImageRepository.save(remaining.get(0));
                remaining = roomImageRepository.findByRoomId(roomId);
            }
        }

        List<RoomImageResponse> resp = new ArrayList<>();
        if (remaining != null) {
            for (RoomImage r : remaining) {
                resp.add(new RoomImageResponse(r.getId(), r.getImageUrl(), r.getIsThumbnail()));
            }
        }
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "Room image deleted", resp));
    }
}