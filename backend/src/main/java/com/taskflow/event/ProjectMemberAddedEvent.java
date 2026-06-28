package com.taskflow.event;

import com.taskflow.domain.entity.Project;
import com.taskflow.domain.entity.User;

public record ProjectMemberAddedEvent(Project project, User member, User addedBy) {
}
