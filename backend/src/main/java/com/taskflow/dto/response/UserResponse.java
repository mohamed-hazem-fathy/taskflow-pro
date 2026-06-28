package com.taskflow.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        String avatarUrl,
        boolean isActive,
        List<String> roles,
        LocalDateTime createdAt
) {
}
