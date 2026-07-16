package com.sarthak.teamcollab.service;

import org.springframework.stereotype.Service;

import com.sarthak.teamcollab.dto.DashboardStatsResponse;
import com.sarthak.teamcollab.repository.ProjectRepository;
import com.sarthak.teamcollab.repository.TaskRepository;

@Service
public class DashboardService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DashboardService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public DashboardStatsResponse getDashboardStats() {
        long totalProjects = projectRepository.countByDeletedFalse();
    }
}
