package com.sarthak.teamcollab.controller;

import com.sarthak.teamcollab.dto.UserProfileRequest;
import com.sarthak.teamcollab.dto.UserResponse;
import com.sarthak.teamcollab.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserProfileRequest request, Principal principal) {
                String email = principal.getName();
        UserResponse response = userService.updateProfile(email, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam String role, Principal principal) {
                String adminEmail = principal.getName();
        UserResponse response = userService.updateUserRole(id, role, adminEmail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestParam boolean active,
            Principal principal) {
                String adminEmail = principal.getName();
        UserResponse response = userService.updateUserStatus(id, active, adminEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/grouped")
    public ResponseEntity<java.util.Map<String, java.util.List<UserResponse>>> getGroupedUsers() {
        return ResponseEntity.ok(userService.getGroupedUsers());
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> searchAndFilterUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<UserResponse> response = userService.searchAndFilterUsers(keyword, role, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
}
