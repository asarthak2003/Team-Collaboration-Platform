package com.sarthak.teamcollab.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sarthak.teamcollab.dto.ActivityLogResponse;
import com.sarthak.teamcollab.model.ActivityLog;
import com.sarthak.teamcollab.model.User;
import com.sarthak.teamcollab.repository.ActivityLogRepository;

@Service
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    private ActivityLogResponse mapToResponse(ActivityLog log) {
        return new ActivityLogResponse(log.getId(), log.getUser().getId(), log.getUser().getName(), log.getAction(),
                log.getEntityType(), log.getEntityId(), log.getCreatedAt());
    }

    @Transactional
    public void logAction(User user, String action, String entityType, Long entityId) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        activityLogRepository.save(log);
    }

    public List<ActivityLogResponse> getLogsForEntity(String entityType, Long entityId) {
        return activityLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ActivityLogResponse> getAllLogs() {
        return activityLogRepository.findAllByOrderByCreatedAtDesc().stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
