package com.taskflow.repository.specification;

import com.taskflow.domain.entity.Project;
import com.taskflow.domain.entity.Task;
import com.taskflow.domain.enums.TaskPriority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.dto.TaskFilterParams;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> withFilters(Project project, TaskFilterParams params) {
        return Specification
                .where(belongsToProject(project))
                .and(hasStatus(params.getStatus()))
                .and(hasPriority(params.getPriority()))
                .and(hasAssignee(params.getAssigneeId()))
                .and(dueDateBefore(params.getDueDateBefore()))
                .and(dueDateAfter(params.getDueDateAfter()))
                .and(searchInTitleOrDescription(params.getSearch()));
    }

    private static Specification<Task> belongsToProject(Project project) {
        return (root, query, cb) -> cb.equal(root.get("project"), project);
    }

    private static Specification<Task> hasStatus(TaskStatus status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Task> hasPriority(TaskPriority priority) {
        return priority == null ? null : (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    private static Specification<Task> hasAssignee(UUID assigneeId) {
        return assigneeId == null ? null : (root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    private static Specification<Task> dueDateBefore(LocalDate dueDateBefore) {
        return dueDateBefore == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dueDate"), dueDateBefore);
    }

    private static Specification<Task> dueDateAfter(LocalDate dueDateAfter) {
        return dueDateAfter == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateAfter);
    }

    private static Specification<Task> searchInTitleOrDescription(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String term = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), term),
                cb.like(cb.lower(root.get("description")), term)
        );
    }
}
