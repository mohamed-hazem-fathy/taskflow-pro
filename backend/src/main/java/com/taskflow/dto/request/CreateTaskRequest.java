package com.taskflow.dto.request;

import com.taskflow.domain.enums.TaskPriority;
import com.taskflow.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank String title,
        String description,
        TaskPriority priority,
        TaskStatus status,
        @NotNull UUID projectId,
        UUID assigneeId,
        LocalDate dueDate,
        Integer estimatedHours
) {
    public CreateTaskRequest {
        if (priority == null) {
            priority = TaskPriority.MEDIUM;
        }
        if (status == null) {
            status = TaskStatus.TODO;
        }
    }
}
