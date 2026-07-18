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

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
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
    }

}