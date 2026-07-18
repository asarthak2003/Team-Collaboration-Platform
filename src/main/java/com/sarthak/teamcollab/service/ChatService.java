package com.sarthak.teamcollab.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sarthak.teamcollab.dto.ChatMessageResponse;
import com.sarthak.teamcollab.model.ChatMessage;
import com.sarthak.teamcollab.repository.ChatMessageRepository;
import com.sarthak.teamcollab.repository.TaskRepository;
import com.sarthak.teamcollab.repository.UserRepository;

@Service
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, TaskRepository taskRepository,
            UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        return new ChatMessageResponse(message.getId(), message.getTask().getId(), message.getSender().getName(),
                message.getContent(), message.getSentAt());
    }

    @Transactional
    public ChatMessageResponse saveMessage(Long taskId, String email, String content) {

    }
}
