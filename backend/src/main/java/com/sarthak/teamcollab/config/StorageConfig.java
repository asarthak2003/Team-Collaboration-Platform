package com.sarthak.teamcollab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.sarthak.teamcollab.service.FileStorageService;
import com.sarthak.teamcollab.service.LocalFileStorageService;
import com.sarthak.teamcollab.service.CloudinaryFileStorageService;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "cloudinary")
    @Primary
    public FileStorageService cloudinaryFileStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        return new CloudinaryFileStorageService(cloudName, apiKey, apiSecret);
    }

    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
    public FileStorageService localFileStorageService() {
        return new LocalFileStorageService();
    }
}
