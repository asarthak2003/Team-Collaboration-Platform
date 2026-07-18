package com.sarthak.teamcollab.service;

import com.sarthak.teamcollab.exception.BadRequestException;
import com.sarthak.teamcollab.exception.ResourceNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sarthak.teamcollab.config.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        String roleName = request.getRoleName().toUpperCase();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        String finalRoleName = roleName;

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role " + finalRoleName + " not found"));

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser.getEmail(), savedUser.getRole().getName());
        return new AuthResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(),
                savedUser.getRole().getName(), "Registration successful", token);
    }

    public AuthResponse loginUser(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new BadRequestException("Invalid username/email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username/email or password");
        }
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().getName());
        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                "Login successful", token);
    }
}
