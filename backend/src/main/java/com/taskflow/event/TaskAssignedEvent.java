package com.taskflow.event;

import com.taskflow.domain.entity.Task;
import com.taskflow.domain.entity.User;

public record TaskAssignedEvent(Task task, User assignee, User assignedBy) {
}
