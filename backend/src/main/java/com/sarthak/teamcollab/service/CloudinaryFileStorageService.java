package com.sarthak.teamcollab.service;

import org.springframework.beans.factory.annotation.Value;

import com.cloudinary.Cloudinary;

public class CloudinaryFileStorageService implements FileStorageService {
    private final Cloudinary cloudinary;

    public CloudinaryFileStorageService(@Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey, @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(
                ObjectUtils.asMap("cloud_name", cloudName, "api_key", apiKey, "api_secret", apiSecret));
    }

}
