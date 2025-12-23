package com.example.rental.utils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads";

    public String storeFile(MultipartFile file, String subfolder) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR, subfolder);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public boolean deleteFile(String subfolder, String filename) {
        if (filename == null || filename.isBlank()) return false;
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR, subfolder);
            Path filePath = uploadPath.resolve(filename);
            return Files.deleteIfExists(filePath);
        } catch (Exception ex) {
            return false;
        }
    }
}
