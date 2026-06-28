package com.taskflow.dto.response;

import com.taskflow.domain.enums.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        ProjectStatus status,
        UserSummaryResponse owner,
        LocalDate startDate,
        LocalDate endDate,
        int memberCount,
        int taskCount,
        LocalDateTime createdAt
) {
}
