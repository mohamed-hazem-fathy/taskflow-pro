package com.taskflow.dto.request;

import com.taskflow.domain.enums.ProjectStatus;

import java.time.LocalDate;

public record UpdateProjectRequest(
        String name,
        String description,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate
) {
}
