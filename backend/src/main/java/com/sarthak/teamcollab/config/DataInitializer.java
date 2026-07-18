package com.sarthak.teamcollab.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sarthak.teamcollab.model.Role;
import com.sarthak.teamcollab.repository.RoleRepository;

// Implements Spring Boot's CommandLineRunner, which runs custom startup code as soon as the application context is loaded.
// Checks if the roles (ROLE_ADMIN, ROLE_PROJECT_MANAGER, ROLE_MEMBER) exist in the roles table. If not, it saves them to the database.

@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> defaultRoles = Arrays.asList("ROLE_ADMIN", "ROLE_PROJECT_MANAGER", "ROLE_MEMBER");
        for (String roleName : defaultRoles) {
            if (roleRepository.findByName((roleName)).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }

        // 3. Create Admin User if not exists
        String adminEmail = "[EMAIL_ADDRESS]";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setUsername("admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setActive(true);

            Optional<Role> adminRoleOpt = roleRepository.findByName("ROLE_ADMIN");
            if (adminRoleOpt.isPresent()) {
                admin.setRoles(Collections.singleton(adminRoleOpt.get()));
            } else {
                // Fallback: try to find by ID if somehow name lookup failed despite creation
                // But since we just created it, findByName should work.
                // If not, this is a deeper issue.
                // For robustness in initialization, we assume role exists.
                throw new RuntimeException("ROLE_ADMIN not found after creation!");
            }

            userRepository.save(admin);
            System.out.println("Admin user created.");
        }
    }

}