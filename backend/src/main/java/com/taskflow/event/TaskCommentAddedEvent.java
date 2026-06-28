package com.taskflow.event;

import com.taskflow.domain.entity.Task;
import com.taskflow.domain.entity.TaskComment;
import com.taskflow.domain.entity.User;

public record TaskCommentAddedEvent(Task task, TaskComment comment, User author) {
}
