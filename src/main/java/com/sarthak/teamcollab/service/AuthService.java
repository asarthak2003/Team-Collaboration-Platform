package com.sarthak.teamcollab.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sarthak.teamcollab.dto.AuthResponse;
import com.sarthak.teamcollab.dto.LoginRequest;
import com.sarthak.teamcollab.dto.RegisterRequest;
import com.sarthak.teamcollab.model.Role;
import com.sarthak.teamcollab.model.User;
import com.sarthak.teamcollab.repository.RoleRepository;
import com.sarthak.teamcollab.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        String roleName = request.getRoleName().toUpperCase();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        String finalRoleName = roleName;

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role " + finalRoleName + " not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User savedUser = userRepository.save(user);
        return new AuthResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(),
                savedUser.getRole().getName(), "Registration successful");
    }

    public AuthResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                "Login successful");
    }
}
