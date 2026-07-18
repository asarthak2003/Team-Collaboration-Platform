package com.sarthak.teamcollab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    
    private String currentPassword;
    
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}
