package com.cuoiky.Nhom13.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

@Service
public class JobStorageService {
    private final Path rootPath;

    public JobStorageService(@Value("${app.storage.upload-dir:uploads}") String uploadDir) throws IOException {
        this.rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(rootPath);
    }

    public StoredFile storeImage(Long jobId, MultipartFile file) {
        validateImageContentType(file.getContentType());
        String originalFileName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "image.png";
        String safeName = sanitizeFileName(originalFileName);
        String storedName = UUID.randomUUID() + "-" + safeName;
        Path jobFolder = createJobFolder(jobId, "images");
        Path target = jobFolder.resolve(storedName);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store image file", ex);
        }
        return new StoredFile(originalFileName, file.getContentType(), target.toString());
    }

    public StoredFile storeSignature(Long jobId, String imageData) {
        if (!StringUtils.hasText(imageData) || !imageData.startsWith("data:image/png;base64,")) {
            throw new IllegalArgumentException("Signature must be a PNG data URL");
        }
        byte[] data;
        try {
            data = Base64.getDecoder().decode(imageData.substring("data:image/png;base64,".length()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Signature data is invalid");
        }
        Path jobFolder = createJobFolder(jobId, "signatures");
        Path target = jobFolder.resolve("signature-" + UUID.randomUUID() + ".png");
        try {
            Files.write(target, data);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store signature", ex);
        }
        return new StoredFile(target.getFileName().toString(), "image/png", target.toString());
    }

    public StoredFile storeSignature(Long jobId, MultipartFile file) {
        validateImageContentType(file.getContentType());
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read signature file", ex);
        }
        Path jobFolder = createJobFolder(jobId, "signatures");
        Path target = jobFolder.resolve("signature-" + UUID.randomUUID() + ".png");
        try {
            Files.write(target, data);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store signature", ex);
        }
        String originalFileName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : target.getFileName().toString();
        return new StoredFile(originalFileName, "image/png", target.toString());
    }

    public Resource loadAsResource(String storagePath) {
        Path path = Paths.get(storagePath).toAbsolutePath().normalize();
        if (!path.startsWith(rootPath)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new IllegalArgumentException("File not found");
        }
        return resource;
    }

    public void deleteFile(String storagePath) {
        Path path = Paths.get(storagePath).toAbsolutePath().normalize();
        if (!path.startsWith(rootPath)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to delete file", ex);
        }
    }

    public void deleteSignature(String storagePath) {
        deleteFile(storagePath);
    }

    private Path createJobFolder(Long jobId, String child) {
        Path folder = rootPath.resolve("jobs").resolve(String.valueOf(jobId)).resolve(child);
        try {
            Files.createDirectories(folder);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create storage folder", ex);
        }
        return folder;
    }

    private void validateImageContentType(String contentType) {
        if (!StringUtils.hasText(contentType) || !(contentType.equalsIgnoreCase("image/png")
                || contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/jpg")
                || contentType.equalsIgnoreCase("image/webp"))) {
            throw new IllegalArgumentException("Only PNG, JPG, JPEG, WEBP images are supported");
        }
    }

    private String sanitizeFileName(String fileName) {
        return Paths.get(fileName).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public record StoredFile(String originalFileName, String contentType, String storagePath) {
    }
}
