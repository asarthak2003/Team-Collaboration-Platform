package com.sarthak.teamcollab.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sarthak.teamcollab.dto.TaskRequest;
import com.sarthak.teamcollab.dto.TaskResponse;
import com.sarthak.teamcollab.model.Project;
import com.sarthak.teamcollab.model.Task;
import com.sarthak.teamcollab.model.TaskPriority;
import com.sarthak.teamcollab.model.TaskStatus;
import com.sarthak.teamcollab.model.User;
import com.sarthak.teamcollab.repository.ProjectRepository;
import com.sarthak.teamcollab.repository.TaskRepository;
import com.sarthak.teamcollab.repository.UserRepository;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
            UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private User validateAdmin(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User Not Found"));
        String role = user.getRole().getName();
        if (!"ROLE_ADMIN".equals(role)) {
            throw new RuntimeException("Access denied: Only Admin can perform this action");
        }
        return user;
    }

    private TaskResponse mapToResponse(Task task) {
        Long assignedId = task.getAssignedUser() != null ? task.getAssignedUser().getId() : null;
        String assigneeName = task.getAssignedUser() != null ? task.getAssignedUser().getName() : null;
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus().name(),
                task.getPriority().name(), task.getProject().getId(), task.getProject().getName(), assignedId,
                assigneeName, task.getDueDate(), task.getCreatedBy().getId(), task.getCreatedBy().getName(),
                task.getCreatedAt(), task.getUpdatedAt(), task.isDeleted());
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request, String userEmail) {
        User creator = validateAdmin(userEmail);
        Project project = projectRepository.findByIdAndDeletedFalse(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found or deleted"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        task.setCreatedBy(creator);

        if (request.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getPriority() != null) {
            task.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
        }
        if (request.getAssignedUserId() != null) {
            User assignee = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
            task.setAssignedUser(assignee);
        }
        task.setDueDate(request.getDueDate());

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Task not found or deleted"));
        String role = user.getRole().getName();
        if ("ROLE_MEMBER".equals(role)) {
            if (task.getAssignedUser() == null || !task.getAssignedUser().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied: Members can only update their own assigned tasks");
            }
            if (request.getStatus() != null) {
                task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
            }
        } else if ("ROLE_ADMIN".equals(role) || "ROLE_PROJECT_MANAGER".equals(role)) {
            if (request.getTitle() != null)
                task.setTitle(request.getTitle());
            if (request.getDescription() != null)
                task.setDescription(request.getDescription());
            if (request.getStatus() != null) {
                task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
            }
            if (request.getPriority() != null) {
                task.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
            }
            if (request.getAssignedUserId() != null) {
                User assignee = userRepository.findById(request.getAssignedUserId())
                        .orElseThrow(() -> new RuntimeException("Assignee user not found"));
                task.setAssignedUser(assignee);
            }
            task.setDueDate(request.getDueDate());
        } else {
            throw new RuntimeException("Access denied");
        }
        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    @Transactional
    public TaskResponse assignTask(Long id, Long userId, String userEmail) {
        validateAdmin(userEmail);
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Task not found or deleted"));
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Assignee user not found"));

        task.setAssignedUser(assignee);
        Task saved = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse deleteTask(Long id, String userEmail) {
        validateAdmin(userEmail);
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Task not found or already deleted"));
        task.setDeleted(true);
        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    @Transactional
    public TaskResponse restoreTask(Long id, String userEmail) {
        validateAdmin(userEmail);
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.isDeleted()) {
            throw new RuntimeException("Task is not deleted");
        }
        task.setDeleted(false);
        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    public List<TaskResponse> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectIdAndDeletedFalse(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksAssignedToUser(Long userId) {
        return taskRepository.findByAssignedUserIdAndDeletedFalse(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
