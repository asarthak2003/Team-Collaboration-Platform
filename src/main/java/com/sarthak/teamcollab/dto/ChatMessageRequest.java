package com.sarthak.teamcollab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

//what the frontend sends to the WebSocket
@Data
public class ChatMessageRequest {
    @NotNull(message = "Task ID is required")
    private Long taskId;
    @NotBlank(message = "Message content cannot be empty")
    private String content;
}
