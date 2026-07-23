package com.sarthak.teamcollab.service;

import com.sarthak.teamcollab.exception.FileStorageException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

//Local filesystem implementation of the FileStorageService.

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path fileStorageLocation;

    public LocalFileStorageService() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.",
                    ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        // Normalize file name and add UUID to prevent overwriting
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
        try {
            if (fileName.contains("..")) {
                throw new IOException("Sorry! Filename contains invalid path sequence");
            }
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

            // Ensures that path remains inside base upload directory (prevents directory
            // traversal attacks)
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new FileStorageException("Access Denied: File path is outside designated uploads directory");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found " + fileName, ex);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (filePath.startsWith(this.fileStorageLocation)) {
                java.nio.file.Files.deleteIfExists(filePath);
            }
        } catch (java.io.IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName, ex);
        }
    }
}
