package com.sarthak.teamcollab.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sarthak.teamcollab.dto.ProjectResponse;
import com.sarthak.teamcollab.service.ProjectService;
import java.util.Collections;
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
public class ProjectControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    @WithMockUser(username = "pm@test.com", roles = {"PROJECT_MANAGER"})
    void getAllActiveProjects_WithKeyword_ReturnsOk() throws Exception {
        ProjectResponse project = new ProjectResponse(1L, "StoreSmith", "Desc", "ACTIVE", 2L, "Manager 1", null, null, false);
        when(projectService.getAllActiveProjects("Store")).thenReturn(Collections.singletonList(project));

        mockMvc.perform(get("/api/projects")
                .param("keyword", "Store")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("StoreSmith"));
    }

    @Test
    void getProjects_WithoutAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
