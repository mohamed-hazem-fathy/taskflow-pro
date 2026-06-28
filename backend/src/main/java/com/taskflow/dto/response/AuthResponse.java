package com.taskflow.dto.response;

import java.util.List;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        String email,
        String fullName,
        List<String> roles
) {
    public AuthResponse(String token, String username, String email, String fullName, List<String> roles) {
        this(token, "Bearer", username, email, fullName, roles);
    }
}
