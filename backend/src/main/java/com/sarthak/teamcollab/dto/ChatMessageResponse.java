package com.sarthak.teamcollab.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//what the WebSocket broadcasts back to all connected users
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long taskId;
    private String senderUsername;
    private String content;
    private LocalDateTime sentAt;
}
