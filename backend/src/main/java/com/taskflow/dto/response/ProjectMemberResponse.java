package com.taskflow.dto.response;

import com.taskflow.domain.enums.ProjectRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID id,
        UserSummaryResponse user,
        ProjectRole role,
        LocalDateTime joinedAt
) {
}
