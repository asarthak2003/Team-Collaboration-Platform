package com.sarthak.teamcollab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskRequest {
    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private String status; // e.g. TODO, IN_PROGRESS

    private String priority; // e.g. LOW, MEDIUM, HIGH, CRITICAL

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assignedUserId;

    private LocalDate dueDate;
}
