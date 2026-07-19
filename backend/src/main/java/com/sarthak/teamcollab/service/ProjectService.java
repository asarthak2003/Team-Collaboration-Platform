package com.sarthak.teamcollab.service;

import org.springframework.security.access.AccessDeniedException;
import com.sarthak.teamcollab.exception.BadRequestException;
import com.sarthak.teamcollab.exception.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sarthak.teamcollab.dto.ProjectRequest;
import com.sarthak.teamcollab.dto.ProjectResponse;
import com.sarthak.teamcollab.model.Project;
import com.sarthak.teamcollab.model.User;
import com.sarthak.teamcollab.repository.ProjectRepository;
import com.sarthak.teamcollab.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService; // added activitylog so that when user updates, creates it is
                                                         // stored in db

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
            ActivityLogService activityLogService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    private User validateAdminOrProjectManager(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String role = user.getRole().getName();
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_PROJECT_MANAGER".equals(role)) {
            throw new AccessDeniedException("ACCESS DENIED: Only ADMINS or PROJECT MANAGERS can perform this action.");
        }
        return user;
    }

    private ProjectResponse mapToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedBy().getId(),
                project.getCreatedBy().getName(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.isDeleted());
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String userEmail) {
        User admin = validateAdminOrProjectManager(userEmail);
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            project.setStatus(request.getStatus());
        }
        project.setCreatedBy(admin);
        Project saved = projectRepository.save(project);
        activityLogService.logAction(admin, "PROJECT_CREATED", "PROJECT", saved.getId());// creation log
        return mapToResponse(saved);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, String userEmail) {
        User user = validateAdminOrProjectManager(userEmail);
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found or deleted"));
        validateProjectOwnership(project, user);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            project.setStatus(request.getStatus());
        }
        Project updated = projectRepository.save(project);
        activityLogService.logAction(user, "PROJECT_UPDATED", "PROJECT", updated.getId()); // updation log
        return mapToResponse(updated);
    }

    @Transactional
    public ProjectResponse deleteProject(Long id, String userEmail) {
        User user = validateAdminOrProjectManager(userEmail);
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found or already deleted"));
        validateProjectOwnership(project, user);
        project.setDeleted(true);
        Project saved = projectRepository.save(project);
        activityLogService.logAction(user, "PROJECT_DELETED", "PROJECT", saved.getId()); // deletion log
        return mapToResponse(saved);
    }



    @Transactional
    public ProjectResponse restoreProject(Long id, String userEmail) {
        User user = validateAdminOrProjectManager(userEmail);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        validateProjectOwnership(project, user);
        project.setDeleted(false);
        project.setStatus("ACTIVE");
        Project saved = projectRepository.save(project);
        activityLogService.logAction(user, "PROJECT_RESTORED", "PROJECT", saved.getId()); // restore log
        return mapToResponse(saved);
    }

    @Transactional
    public ProjectResponse archiveProject(Long id, String userEmail) {
        User user = validateAdminOrProjectManager(userEmail);
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        validateProjectOwnership(project, user);
        project.setStatus("ARCHIVED");
        Project saved = projectRepository.save(project);
        activityLogService.logAction(user, "PROJECT_ARCHIVED", "PROJECT", saved.getId()); // archive log
        return mapToResponse(saved);
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found or deleted"));
        return mapToResponse(project);
    }

    public List<ProjectResponse> getAllActiveProjects(String keyword) {
        List<Project> projects = projectRepository.findByDeletedFalse();
        if (keyword != null && !keyword.isBlank()) {
            String lower = keyword.toLowerCase();
            return projects.stream()
                    .filter(p -> p.getName().toLowerCase().contains(lower) || 
                                 (p.getDescription() != null && p.getDescription().toLowerCase().contains(lower)))
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateProjectOwnership(Project project, User user) {
        if ("ROLE_PROJECT_MANAGER".equals(user.getRole().getName())) {
            if (!project.getCreatedBy().getId().equals(user.getId())) {
                throw new BadRequestException("You do not have permission to manage this project.");
            }
        }
    }

}
