package com.taskflow.event;

import com.taskflow.domain.enums.NotificationType;
import com.taskflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final NotificationService notificationService;

    @Async("taskExecutor")
    @EventListener
    public void onTaskAssigned(TaskAssignedEvent event) {
        String projectName = event.task().getProject().getName();
        String message = String.format(
                "Task '%s' in project '%s' was assigned to you by %s",
                event.task().getTitle(),
                projectName,
                event.assignedBy().getFullName()
        );

        notificationService.createNotification(
                event.assignee(),
                NotificationType.TASK_ASSIGNED,
                "You have been assigned a task",
                message,
                event.task().getId(),
                "TASK"
        );
    }

    @Async("taskExecutor")
    @EventListener
    public void onTaskStatusChanged(TaskStatusChangedEvent event) {
        if (event.task().getReporter().getId().equals(event.changedBy().getId())) {
            return;
        }

        String message = String.format(
                "Task '%s' status changed from %s to %s",
                event.task().getTitle(),
                event.oldStatus(),
                event.newStatus()
        );

        notificationService.createNotification(
                event.task().getReporter(),
                NotificationType.TASK_STATUS_CHANGED,
                "Task status updated",
                message,
                event.task().getId(),
                "TASK"
        );
    }

    @Async("taskExecutor")
    @EventListener
    public void onCommentAdded(TaskCommentAddedEvent event) {
        if (event.task().getAssignee() == null
                || event.task().getAssignee().getId().equals(event.author().getId())) {
            return;
        }

        String message = String.format(
                "%s commented on '%s'",
                event.author().getFullName(),
                event.task().getTitle()
        );

        notificationService.createNotification(
                event.task().getAssignee(),
                NotificationType.TASK_COMMENT_ADDED,
                "New comment on your task",
                message,
                event.task().getId(),
                "TASK"
        );
    }

    @Async("taskExecutor")
    @EventListener
    public void onProjectMemberAdded(ProjectMemberAddedEvent event) {
        String message = String.format(
                "You were added to project '%s' by %s",
                event.project().getName(),
                event.addedBy().getFullName()
        );

        notificationService.createNotification(
                event.member(),
                NotificationType.PROJECT_MEMBER_ADDED,
                "You've been added to a project",
                message,
                event.project().getId(),
                "PROJECT"
        );
    }
}
