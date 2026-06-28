package com.taskflow.dto;

import com.taskflow.domain.enums.TaskPriority;
import com.taskflow.domain.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class TaskFilterParams {

    private TaskStatus status;
    private TaskPriority priority;
    private UUID assigneeId;
    private LocalDate dueDateBefore;
    private LocalDate dueDateAfter;
    private String search;
}
