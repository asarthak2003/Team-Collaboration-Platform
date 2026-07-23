package com.sarthak.teamcollab.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sarthak.teamcollab.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

public class CloudinaryFileStorageService implements FileStorageService {
    private final Cloudinary cloudinary;

    public CloudinaryFileStorageService(@Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey, @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(
                ObjectUtils.asMap("cloud_name", cloudName, "api_key", apiKey, "api_secret", apiSecret));
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file in Cloudinary", e);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            return new UrlResource(fileName);
        } catch (MalformedURLException e) {
            throw new FileStorageException("File not found: " + fileName, e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            String publicId = extractPublicId(fileName);
            String resourceType = extractResourceType(fileName);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
        } catch (Exception e) {
            throw new FileStorageException("Could not delete file from Cloudinary: " + fileName, e);
        }
    }

    private String extractPublicId(String url) {
        if (url == null || !url.contains("/upload/")) {
            return url;
        }
        String afterUpload = url.substring(url.indexOf("/upload/") + 8);
        if (afterUpload.matches("^v\\d+/.*")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }
        int dotIdx = afterUpload.lastIndexOf('.');
        if (dotIdx != -1) {
            afterUpload = afterUpload.substring(0, dotIdx);
        }
        return afterUpload;
    }

    private String extractResourceType(String url) {
        if (url == null || !url.contains("/upload/")) {
            return "image";
        }
        String[] parts = url.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("upload".equals(parts[i]) && i > 0) {
                return parts[i - 1];
            }
        }
        return "image";
    }
}