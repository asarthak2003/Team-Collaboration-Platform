package com.sarthak.teamcollab.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sarthak.teamcollab.dto.ChatMessageRequest;
import com.sarthak.teamcollab.dto.ChatMessageResponse;
import com.sarthak.teamcollab.service.ChatService;

@RestController
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest chatMessageRequest, Principal principal) {
        // identify sender & save message to db
        String email = principal.getName();
        ChatMessageResponse response = chatService.saveMessage(chatMessageRequest.getTaskId(), email,
                chatMessageRequest.getContent());

        // broadcast the saved message to all users subscribed to this task's specific
        // chat room
        messagingTemplate.convertAndSend("/topic/task/" + chatMessageRequest.getTaskId(), response);
    }

    // fetch chat history when user first open a task window
    @GetMapping("/api/tasks/{taskId}/chat")
    public List<ChatMessageResponse> getChatHistory(@PathVariable Long taskId) {
        return chatService.getChatHistory(taskId);
    }

}
