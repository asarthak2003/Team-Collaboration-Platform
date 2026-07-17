package com.sarthak.teamcollab.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sarthak.teamcollab.dto.UserProfileRequest;
import com.sarthak.teamcollab.dto.UserResponse;
import com.sarthak.teamcollab.model.Role;
import com.sarthak.teamcollab.model.User;
import com.sarthak.teamcollab.repository.RoleRepository;
import com.sarthak.teamcollab.repository.UserRepository;
import com.sarthak.teamcollab.repository.UserSpecification;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
    }

    private User validateAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!"ROLE_ADMIN".equals(user.getRole().getName())) {
            throw new RuntimeException("Access denied: Only Admin can perform this action");
        }
        return user;
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole().getName(),
                user.isActive(), user.getCreatedAt());
    }

    @Transactional
    public UserResponse updateProfile(String email, UserProfileRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        activityLogService.logAction(saved, "PROFILE_UPDATED", "USER", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public UserResponse updateUserRole(Long id, String roleName, String adminEmail) {
        User admin = validateAdmin(adminEmail);
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String formattedRoleName = roleName.toUpperCase();
        if (!formattedRoleName.startsWith("ROLE_")) {
            formattedRoleName = "ROLE_" + formattedRoleName;
        }
        String finalRoleName = formattedRoleName;
        Role role = roleRepository.findByName(formattedRoleName)
                .orElseThrow(() -> new RuntimeException("Role " + finalRoleName + " not found"));
        targetUser.setRole(role);
        User saved = userRepository.save(targetUser);
        activityLogService.logAction(admin, "USER_ROLE_UPDATED", "USER", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public UserResponse updateUserStatus(Long id, boolean active, String adminEmail) {
        User admin = validateAdmin(adminEmail);
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        targetUser.setActive(active);
        User saved = userRepository.save(targetUser);
        String action = active ? "USER_ACTIVATED" : "USER_DEACTIVATED";
        activityLogService.logAction(admin, action, "USER", saved.getId());
        return mapToResponse(saved);
    }

    public List<UserResponse> searchAndFilterUsers(String keyword, String roleName, int page, int size,
            String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<User> spec = UserSpecification.filterUsers(keyword, roleName);
        return userRepository.findAll(spec, pageable).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
