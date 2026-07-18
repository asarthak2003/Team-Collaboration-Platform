package com.sarthak.teamcollab.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sarthak.teamcollab.dto.CommentRequest;
import com.sarthak.teamcollab.dto.CommentResponse;
import com.sarthak.teamcollab.service.CommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long taskId, @Valid @RequestBody CommentRequest request,
            Principal principal) {
                String userEmail = principal.getName();
        CommentResponse response = commentService.addcomment(taskId, request, userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<?> getCommentsByTask(@PathVariable Long taskId) {
                List<CommentResponse> response = commentService.getCommentsByTask(taskId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<?> editComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request,
            Principal principal) {
                String userEmail = principal.getName();
        CommentResponse response = commentService.editComment(id, request, userEmail);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, Principal principal) {
                String userEmail = principal.getName();
        commentService.deleteComment(id, userEmail);
        return ResponseEntity.ok(new AuthController.ErrorResponse("Comment deleted successfully"));
    }

    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}