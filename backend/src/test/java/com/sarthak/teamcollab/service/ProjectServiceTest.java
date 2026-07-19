package com.sarthak.teamcollab.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sarthak.teamcollab.repository.ProjectRepository;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private ProjectService projectService;

    private User manager1;
    private User manager2;
    private User admin;
    private Project project1;

    @BeforeEach
    void setup() {
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        Role managerRole = new Role();
        managerRole.setName("ROLE_PROJECT_MANAGER");

        admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setRole(adminRole);

        manager1 = new User();
        manager1.setId(2L);
        manager1.setEmail("pm1@test.com");
        manager1.setRole(managerRole);

        manager2 = new User();
        manager2.setId(3L);
        manager2.setEmail("pm2@test.com");
        manager2.setRole(managerRole);
        project1 = new Project();

        project1.setId(10L);
        project1.setName("StoreSmith");
        project1.setCreatedBy(manager1);
        project1.setDeleted(false);
    }

}
