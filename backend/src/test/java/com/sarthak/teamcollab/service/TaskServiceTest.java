package com.sarthak.teamcollab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sarthak.teamcollab.dto.TaskRequest;
import com.sarthak.teamcollab.dto.TaskResponse;
import com.sarthak.teamcollab.exception.BadRequestException;
import com.sarthak.teamcollab.model.*;
import com.sarthak.teamcollab.repository.ProjectRepository;
import com.sarthak.teamcollab.repository.TaskRepository;
import com.sarthak.teamcollab.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskService taskService;

    private User manager1;
    private User manager2;
    private Project activeProject;
    private Project archivedProject;
    private Task task1;

    @BeforeEach
    void setUp() {
        Role managerRole = new Role();
        managerRole.setName("ROLE_PROJECT_MANAGER");

        manager1 = new User();
        manager1.setId(2L);
        manager1.setEmail("pm1@test.com");
        manager1.setName("Manager 1");
        manager1.setRole(managerRole);

        manager2 = new User();
        manager2.setId(3L);
        manager2.setEmail("pm2@test.com");
        manager2.setName("Manager 2");
        manager2.setRole(managerRole);

        activeProject = new Project();
        activeProject.setId(10L);
        activeProject.setName("Active Project");
        activeProject.setCreatedBy(manager1);
        activeProject.setStatus("ACTIVE");
        activeProject.setDeleted(false);

        archivedProject = new Project();
        archivedProject.setId(11L);
        archivedProject.setName("Archived Project");
        archivedProject.setCreatedBy(manager1);
        archivedProject.setStatus("ARCHIVED");
        archivedProject.setDeleted(false);

        task1 = new Task();
        task1.setId(20L);
        task1.setTitle("Task 1");
        task1.setProject(activeProject);
        task1.setCreatedBy(manager1);
        task1.setDeleted(false);
    }

    @Test
    void createTask_InArchivedProject_ThrowsBadRequestException() {
        when(userRepository.findByEmail("pm1@test.com")).thenReturn(Optional.of(manager1));
        when(projectRepository.findByIdAndDeletedFalse(11L)).thenReturn(Optional.of(archivedProject));

        TaskRequest request = new TaskRequest();
        request.setProjectId(11L);
        request.setTitle("New Task");

        assertThrows(BadRequestException.class, () -> {
            taskService.createTask(request, "pm1@test.com");
        });

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void assignTask_ToNonOwnedProject_ThrowsAccessDeniedException() {
        when(userRepository.findByEmail("pm2@test.com")).thenReturn(Optional.of(manager2));
        when(taskRepository.findByIdAndDeletedFalse(20L)).thenReturn(Optional.of(task1));

        assertThrows(AccessDeniedException.class, () -> {
            taskService.assignTask(20L, 4L, "pm2@test.com");
        });
    }
}
