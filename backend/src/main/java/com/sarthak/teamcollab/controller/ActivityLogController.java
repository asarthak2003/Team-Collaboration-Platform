package com.sarthak.teamcollab.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sarthak.teamcollab.dto.ActivityLogResponse;
import com.sarthak.teamcollab.service.ActivityLogService;

@RestController
@RequestMapping("/api/activities")
public class ActivityLogController {
    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public ResponseEntity<List<ActivityLogResponse>> getAllLogs() {
        return ResponseEntity.ok(activityLogService.getAllLogs());
    }

    @GetMapping("/project/{id}")
    public ResponseEntity<List<ActivityLogResponse>> getLogsByProject(@PathVariable Long id) {
        return ResponseEntity.ok(activityLogService.getLogsForEntity("PROJECT", id));
    }

    @GetMapping("/task/{id}")
    public ResponseEntity<List<ActivityLogResponse>> getLogsByTask(@PathVariable Long id) {
        return ResponseEntity.ok(activityLogService.getLogsForEntity("TASK", id));
    }
}
