package com.sarthak.teamcollab.service;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sarthak.teamcollab.dto.CommentRequest;
import com.sarthak.teamcollab.dto.CommentResponse;
import com.sarthak.teamcollab.exception.ResourceNotFoundException;
import com.sarthak.teamcollab.model.Comment;
import com.sarthak.teamcollab.model.Task;
import com.sarthak.teamcollab.model.User;
import com.sarthak.teamcollab.repository.CommentRepository;
import com.sarthak.teamcollab.repository.TaskRepository;
import com.sarthak.teamcollab.repository.UserRepository;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService; // logging the users activity
    private final NotificationService notificationService; // for live notification

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository,
            UserRepository userRepository, ActivityLogService activityLogService,
            NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    private CommentResponse mapToResponse(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getTask().getId(), comment.getUser().getId(),
                comment.getUser().getName(), comment.getContent(), comment.getCreatedAt(), comment.getUpdatedAt());
    }

    @Transactional
    public CommentResponse addcomment(Long taskid, CommentRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));
        Task task = taskRepository.findByIdAndDeletedFalse(taskid)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found or deleted"));
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setContent(request.getContent());
        Comment saved = commentRepository.save(comment);
        activityLogService.logAction(user, "COMMENT_ADDED", "TASK", saved.getId());

        // TRIGGER NOTIFICATIONS:

        // 1. Notify the task assignee
        User assignee = task.getAssignedUser();
        if (assignee != null && !assignee.getId().equals(user.getId())) {
            notificationService.createAndSendNotification(
                    assignee,
                    user.getName() + " commented on your task: " + task.getTitle(),
                    "/tasks/" + task.getId());
        }
        // 2. Scan comment text for mentions
        List<User> allUsers = userRepository.findAll();
        for (User u : allUsers) {
            if (u.getId().equals(user.getId())) {
                continue;
            }
            // Check if comment body contains their name or email prefixed with "@"
            boolean isMentioned = request.getContent().contains("@" + u.getName()) ||
                    request.getContent().contains("@" + u.getEmail());
            if (isMentioned) {
                if (assignee == null || !assignee.getId().equals(u.getId())) {
                    notificationService.createAndSendNotification(
                            u,
                            user.getName() + " mentioned you in a comment on: " + task.getTitle(),
                            "/tasks/" + task.getId());
                }
            }
        }

        return mapToResponse(saved);
    }

    @Transactional
    public CommentResponse editComment(Long commentId, CommentRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied: You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        Comment saved = commentRepository.save(comment);
        activityLogService.logAction(user, "COMMENT_UPDATED", "TASK", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        boolean isAuthorized = "ROLE_ADMIN".equals(user.getRole().getName())
                || "ROLE_PROJECT_MANAGER".equals(user.getRole().getName());
        boolean isOwner = comment.getUser().getId().equals(user.getId());
        if (!isAuthorized && !isOwner) {
            throw new AccessDeniedException("Access denied: You can only delete your own comments");
        }
        Long deletedId = comment.getId();
        commentRepository.delete(comment);
        activityLogService.logAction(user, "COMMENT_DELETED", "TASK", deletedId);

    }

    public List<CommentResponse> getCommentsByTask(Long taskId) {
        taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not found or deleted"));

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}