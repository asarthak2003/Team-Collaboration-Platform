package com.sarthak.teamcollab.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sarthak.teamcollab.dto.AttachmentResponse;
import com.sarthak.teamcollab.model.Attachment;
import com.sarthak.teamcollab.model.Project;
import com.sarthak.teamcollab.model.Task;
import com.sarthak.teamcollab.model.User;
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

    private AttachmentResponse mapToResponse(Attachment attachment) {
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/attachments/download/")
                .path(attachment.getFileName()).toUriString();

        return new AttachmentResponse(attachment.getId(), attachment.getOriginalFileName(), attachment.getFileType(),
                attachment.getFileSize(), fileDownloadUri,
                attachment.getTask() != null ? attachment.getTask().getId() : null,
                attachment.getProject() != null ? attachment.getProject().getId() : null,
                attachment.getUploadedBy().getEmail(), attachment.getUploadedAt());
    }

    @Transactional
    public AttachmentResponse uploadAttachment(MultipartFile file, Long projectId, Long taskId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        if (projectId == null && taskId == null) {
            throw new RuntimeException("Attachment must be linked to either a project or a task.");
        }

        Project project = null;
        if (projectId != null) {
            project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
        }

        Task task = null;
        if (taskId != null) {
            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));
        }

        // save file to disk using FileStorageService
        String fileName = fileStorageService.storeFile(file);
        Attachment attachment = new Attachment();
        attachment.setFileName(fileName);
        attachment.setOriginalFileName(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setFilePath(fileName);
        attachment.setProject(project);
        attachment.setTask(task);
        attachment.setUploadedBy(user);

        Attachment saved = attachmentRepository.save(attachment);

        // log the upload activity
        String entityType = task != null ? "TASK" : "PROJECT";
        Long entityId = task != null ? task.getId() : project.getId();
        activityLogService.logAction(user, "FILE_UPLOADED", entityType, entityId);
        return mapToResponse(saved);
    }

    public List<AttachmentResponse> getAttachmentsForTask(Long taskId) {
        return attachmentRepository.findByTaskId(taskId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AttachmentResponse> getAttachmentsForProject(Long projectId) {
        return attachmentRepository.findByProjectId(projectId).stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
