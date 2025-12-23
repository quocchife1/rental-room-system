package com.example.rental.controller;

import com.example.rental.dto.ApiResponseDto;
import com.example.rental.utils.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload/contract")
    public ResponseEntity<ApiResponseDto<String>> uploadContract(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            String fileUrl = fileStorageService.storeFile(file, "contracts");
            return ResponseEntity.ok(ApiResponseDto.success(
                    HttpStatus.OK.value(),
                    "File uploaded successfully",
                    fileUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "File upload failed",
                            e.getMessage(),
                            request.getRequestURI()
                    ));
        }
    }
}
