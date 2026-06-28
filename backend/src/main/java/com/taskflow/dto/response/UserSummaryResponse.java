package com.taskflow.dto.response;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String fullName,
        String username,
        String email,
        String avatarUrl
) {
}
