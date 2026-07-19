package com.sarthak.teamcollab.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String downloadUrl; // URL to fetch the file from our API
    private Long taskId;
    private Long projectId;
    private String uploadedBy;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
}
