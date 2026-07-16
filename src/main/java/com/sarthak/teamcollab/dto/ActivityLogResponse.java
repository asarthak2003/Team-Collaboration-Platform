package com.sarthak.teamcollab.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String action;
    private String entityType;
    private Long entityId;
    private LocalDateTime createdAt;
}
