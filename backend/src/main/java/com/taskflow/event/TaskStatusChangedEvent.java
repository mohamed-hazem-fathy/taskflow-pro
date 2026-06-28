package com.taskflow.event;

import com.taskflow.domain.entity.Task;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.TaskStatus;

public record TaskStatusChangedEvent(Task task, TaskStatus oldStatus, TaskStatus newStatus, User changedBy) {
}
