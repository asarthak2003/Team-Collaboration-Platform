package com.sarthak.teamcollab.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalProjects;
    private long totalTasks;
    private Map<String, Long> taskByStatus;
    private Map<String, Long> taskByPriority;
    private double taskCompletionRate;
    private Map<String, Long> memberTaskDistribution;
}
