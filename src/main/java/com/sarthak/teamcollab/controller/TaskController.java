package com.sarthak.teamcollab.controller;

import com.sarthak.teamcollab.dto.TaskRequest;
import com.sarthak.teamcollab.dto.TaskResponse;
import com.sarthak.teamcollab.model.User;
import com.sarthak.teamcollab.repository.UserRepository;
import com.sarthak.teamcollab.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createTask(
            @Valid @RequestBody TaskRequest request,
            Principal principal) {
        try {
            String userEmail = principal.getName();
            TaskResponse response = taskService.createTask(request, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            Principal principal) {
        try {
            String userEmail = principal.getName();
            TaskResponse response = taskService.updateTask(id, request, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/assign/{userId}")
    public ResponseEntity<?> assignTask(
            @PathVariable Long id,
            @PathVariable Long userId,
            Principal principal) {
        try {
            String userEmail = principal.getName();
            TaskResponse response = taskService.assignTask(id, userId, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(
            @PathVariable Long id,
            Principal principal) {
        try {
            String userEmail = principal.getName();
            TaskResponse response = taskService.deleteTask(id, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreTask(
            @PathVariable Long id,
            Principal principal) {
        try {
            String userEmail = principal.getName();
            TaskResponse response = taskService.restoreTask(id, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponse>> getTasksByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponse>> getTasksAssignedToUser(@PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getTasksAssignedToUser(userId));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<?> getMyTasks(Principal principal) {
        try {
            String userEmail = principal.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(taskService.getTasksAssignedToUser(user.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> searchAndFilterTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<TaskResponse> response = taskService.searchAndFilterTasks(keyword, status, priority, assigneeId, page,
                size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
}
