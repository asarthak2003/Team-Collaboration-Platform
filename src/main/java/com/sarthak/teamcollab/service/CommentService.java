package com.sarthak.teamcollab.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sarthak.teamcollab.dto.CommentRequest;
import com.sarthak.teamcollab.dto.CommentResponse;
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

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository,
            UserRepository userRepository, ActivityLogService activityLogService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    private CommentResponse mapToResponse(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getTask().getId(), comment.getUser().getId(),
                comment.getUser().getName(), comment.getContent(), comment.getCreatedAt(), comment.getUpdatedAt());
    }

    @Transactional
    public CommentResponse addcomment(Long taskid, CommentRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User Not found"));
        Task task = taskRepository.findByIdAndDeletedFalse(taskid)
                .orElseThrow(() -> new RuntimeException("Task not found or deleted"));
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setContent(request.getContent());
        Comment saved = commentRepository.save(comment);
        activityLogService.logAction(user, "COMMENT_ADDED", "TASK", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public CommentResponse editComment(Long commentId, CommentRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        Comment saved = commentRepository.save(comment);
        activityLogService.logAction(user, "COMMENT_UPDATED", "TASK", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole().getName());
        boolean isOwner = comment.getUser().getId().equals(user.getId());
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Access denied: You can only delete your own comments");
        }
        Long deletedId = comment.getId();
        commentRepository.delete(comment);
        activityLogService.logAction(user, "COMMENT_DELETED", "TASK", deletedId);

    }

    public List<CommentResponse> getCommentsByTask(Long taskId) {
        taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new RuntimeException("Task Not found or deleted"));

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}