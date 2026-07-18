package com.sarthak.teamcollab.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sarthak.teamcollab.dto.AttachmentResponse;
import com.sarthak.teamcollab.service.AttachmentService;
import com.sarthak.teamcollab.service.FileStorageService;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {
    private final AttachmentService attachmentService;
    private final FileStorageService fileStorageService;

    public AttachmentController(AttachmentService attachmentService, FileStorageService fileStorageService) {
        this.attachmentService = attachmentService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long projectId, @RequestParam(required = false) Long taskId,
            Principal principal) {
        try {
            String email = principal.getName();
            AttachmentResponse response = attachmentService.uploadAttachment(file, projectId, taskId, email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthController.ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<AttachmentResponse>> getProjectAttachments(@PathVariable Long projectId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsForProject(projectId));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<AttachmentResponse>> getTaskAttachments(@PathVariable Long taskId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsForTask(taskId));
    }

}
