package com.sarthak.teamcollab.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sarthak.teamcollab.dto.NotificationResponse;
import com.sarthak.teamcollab.model.Notification;
import com.sarthak.teamcollab.model.User;
import com.sarthak.teamcollab.repository.NotificationRepository;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void createAndSendNotification(User recipient, String message, String actionUrl) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setActionUrl(actionUrl);
        notification.setRead(false);
        Notification saved = notificationRepository.save(notification);

        NotificationResponse response = mapToResponse(saved);
        messagingTemplate.convertAndSendToUser("/topic/notifications/" + recipient.getEmail(), response);
    }

}
