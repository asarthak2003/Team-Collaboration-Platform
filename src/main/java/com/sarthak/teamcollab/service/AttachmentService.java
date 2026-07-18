package com.sarthak.teamcollab.service;

import org.springframework.stereotype.Service;

import com.sarthak.teamcollab.repository.AttachmentRepository;
import com.sarthak.teamcollab.repository.ProjectRepository;
import com.sarthak.teamcollab.repository.TaskRepository;
import com.sarthak.teamcollab.repository.UserRepository;

@Service
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public AttachmentService(AttachmentRepository attachmentRepository, FileStorageService fileStorageService,
            ProjectRepository projectRepository, TaskRepository taskRepository, UserRepository userRepository,
            ActivityLogService activityLogService) {
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

}
