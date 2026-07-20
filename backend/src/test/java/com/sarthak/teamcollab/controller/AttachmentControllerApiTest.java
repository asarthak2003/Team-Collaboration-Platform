package com.sarthak.teamcollab.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sarthak.teamcollab.service.AttachmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class AttachmentControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttachmentService attachmentService;

    @Test
    @WithMockUser(username = "uploader@test.com")
    void deleteAttachment_AsAuthenticatedUser_ReturnsOk() throws Exception {
        doNothing().when(attachmentService).deleteAttachment(1L, "uploader@test.com");

        mockMvc.perform(delete("/api/attachments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Attachment deleted successfully"));
    }

    @Test
    void deleteAttachment_WithoutAuth_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/attachments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
