package com.taskflow.dto.request;

import com.taskflow.domain.enums.TaskPriority;
import com.taskflow.domain.enums.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskRequest(
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UUID assigneeId,
        LocalDate dueDate,
        Integer estimatedHours
) {
}
