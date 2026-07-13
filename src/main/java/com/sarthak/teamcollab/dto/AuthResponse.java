package com.sarthak.teamcollab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Formats the response sent back to the client after a successful login or registration.
//Returns only the safe details: the user's generated userId, their name, email, the assigned role, and a status message.

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String message;
}