package com.sarthak.teamcollab.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sarthak.teamcollab.dto.AttachmentResponse;
import com.sarthak.teamcollab.service.AttachmentService;
import com.sarthak.teamcollab.service.FileStorageService;

import jakarta.servlet.http.HttpServletRequest;

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
        String email = principal.getName();
        AttachmentResponse response = attachmentService.uploadAttachment(file, projectId, taskId, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<AttachmentResponse>> getProjectAttachments(@PathVariable Long projectId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsForProject(projectId));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<AttachmentResponse>> getTaskAttachments(@PathVariable Long taskId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsForTask(taskId));
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException e) {
                // Fallback if type cannot be determined
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok().contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long id, Principal principal) {
        attachmentService.deleteAttachment(id, principal.getName());
        return ResponseEntity.ok(Map.of("message", "Attachment deleted successfully"));
    }

}
