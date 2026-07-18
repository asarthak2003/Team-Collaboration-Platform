package com.sarthak.teamcollab.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sarthak.teamcollab.model.Role;
import com.sarthak.teamcollab.model.User;
import com.sarthak.teamcollab.repository.RoleRepository;
import com.sarthak.teamcollab.repository.UserRepository;

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

        // Create Admin User if not exists
        String adminEmail = "admin@admin.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not initialized"));
            User admin = new User();
            admin.setName("Admin User");
            admin.setUsername("admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(adminRole);
            admin.setActive(true);

            userRepository.save(admin);
            System.out.println("Default Admin account seeded successfully.");
        }
    }

}