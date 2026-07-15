package com.sarthak.teamcollab.controller;

import com.sarthak.teamcollab.dto.ProjectRequest;
import com.sarthak.teamcollab.dto.ProjectResponse;
import com.sarthak.teamcollab.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<?> createProject(
            @Valid @RequestBody ProjectRequest request,
            Principal principal) {
        try {
            String userEmail = principal.getName();
            ProjectResponse response = projectService.createProject(request, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            Principal principal) {
        try {
            String userEmail = principal.getName();
            ProjectResponse response = projectService.updateProject(id, request, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(
            @PathVariable Long id,
            Principal principal) {
        try {
            String userEmail = principal.getName();
            ProjectResponse response = projectService.deleteProject(id, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreProject(
            @PathVariable Long id,
            Principal principal) {
        try {
            String userEmail = principal.getName();
            ProjectResponse response = projectService.restoreProject(id, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<?> archiveProject(
            @PathVariable Long id,
            Principal principal) {
        try {
            String userEmail = principal.getName();
            ProjectResponse response = projectService.archiveProject(id, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        try {
            ProjectResponse response = projectService.getProjectById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllActiveProjects() {
        return ResponseEntity.ok(projectService.getAllActiveProjects());
    }
}
