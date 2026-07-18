package com.sarthak.teamcollab.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sarthak.teamcollab.dto.DashboardStatsResponse;
import com.sarthak.teamcollab.model.Task;
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
        List<Task> activeTasks = taskRepository.findByDeletedFalse();
        long totalTasks = activeTasks.size();

        // group task by status name
        Map<String, Long> statusMap = activeTasks.stream()
                .collect(Collectors.groupingBy(task -> task.getStatus().name(), Collectors.counting()));

        // grouping task by priority name
        Map<String, Long> priorityMap = activeTasks.stream()
                .collect(Collectors.groupingBy(task -> task.getPriority().name(), Collectors.counting()));

        // calculate task completion rate
        long doneTasks = activeTasks.stream().filter(task -> "DONE".equals(task.getStatus().name())).count();
        double completionRate = totalTasks == 0 ? 0.0 : ((double) doneTasks / totalTasks) * 100.0;

        // group task by username
        Map<String, Long> memberMap = activeTasks.stream()
                .filter(task -> task.getAssignedUser() != null)
                .collect(Collectors.groupingBy(task -> task.getAssignedUser().getName(), Collectors.counting()));

        return new DashboardStatsResponse(totalProjects, totalTasks, statusMap, priorityMap, completionRate, memberMap);
    }
}
