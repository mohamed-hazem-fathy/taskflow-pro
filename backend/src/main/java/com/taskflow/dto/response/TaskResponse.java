package com.taskflow.dto.response;

import com.taskflow.domain.enums.TaskPriority;
import com.taskflow.domain.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UserSummaryResponse assignee,
        UserSummaryResponse reporter,
        LocalDate dueDate,
        Integer estimatedHours,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
