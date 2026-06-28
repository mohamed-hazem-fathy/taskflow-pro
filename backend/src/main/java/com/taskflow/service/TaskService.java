package com.taskflow.service;

import com.taskflow.annotation.Auditable;
import com.taskflow.domain.entity.Project;
import com.taskflow.domain.entity.ProjectMember;
import com.taskflow.domain.entity.Task;
import com.taskflow.domain.entity.TaskComment;
import com.taskflow.domain.entity.TaskHistory;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.ProjectRole;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.dto.TaskFilterParams;
import com.taskflow.dto.request.AddCommentRequest;
import com.taskflow.dto.request.CreateTaskRequest;
import com.taskflow.dto.request.UpdateTaskRequest;
import com.taskflow.dto.response.TaskCommentResponse;
import com.taskflow.dto.response.TaskHistoryResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.event.TaskAssignedEvent;
import com.taskflow.event.TaskCommentAddedEvent;
import com.taskflow.event.TaskStatusChangedEvent;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskCommentRepository;
import com.taskflow.repository.TaskHistoryRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.repository.specification.TaskSpecification;
import com.taskflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Auditable(action = "CREATE_TASK", entityType = "TASK")
    public TaskResponse createTask(CreateTaskRequest request) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.projectId()));
        ensureMember(project, currentUser);

        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.assigneeId()));
            ensureProjectMember(project, assignee);
        }

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .priority(request.priority())
                .project(project)
                .assignee(assignee)
                .reporter(currentUser)
                .dueDate(request.dueDate())
                .estimatedHours(request.estimatedHours())
                .build();

        Task saved = taskRepository.save(task);
        log.info("Task created: id={}, projectId={}, reporter={}", saved.getId(), project.getId(), currentUser.getEmail());
        if (assignee != null) {
            log.info("Task assigned: taskId={}, assignee={}", saved.getId(), assignee.getEmail());
            Hibernate.initialize(saved.getProject());
            eventPublisher.publishEvent(new TaskAssignedEvent(saved, assignee, currentUser));
        }

        return taskMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getProjectTasks(UUID projectId, TaskFilterParams params, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        ensureMember(project, currentUser);

        Specification<Task> spec = TaskSpecification.withFilters(project, params);
        return taskRepository.findAll(spec, pageable).map(taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID taskId) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Task task = findTaskOrThrow(taskId);
        ensureMember(task.getProject(), currentUser);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Task task = findTaskOrThrow(taskId);
        ensureMember(task.getProject(), currentUser);

        List<TaskHistory> historyEntries = new ArrayList<>();
        TaskStatus oldStatus = task.getStatus();
        boolean statusChanged = false;
        boolean assigneeChanged = false;
        User newAssignee = null;

        if (request.title() != null && !request.title().equals(task.getTitle())) {
            historyEntries.add(buildHistory(task, currentUser, "title", task.getTitle(), request.title()));
            task.setTitle(request.title());
        }
        if (request.description() != null && !Objects.equals(request.description(), task.getDescription())) {
            historyEntries.add(buildHistory(task, currentUser, "description", task.getDescription(), request.description()));
            task.setDescription(request.description());
        }
        if (request.status() != null && request.status() != task.getStatus()) {
            historyEntries.add(buildHistory(task, currentUser, "status",
                    task.getStatus().name(), request.status().name()));
            task.setStatus(request.status());
            statusChanged = true;
            if (request.status() == TaskStatus.DONE) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }
        if (request.priority() != null && request.priority() != task.getPriority()) {
            historyEntries.add(buildHistory(task, currentUser, "priority",
                    task.getPriority().name(), request.priority().name()));
            task.setPriority(request.priority());
        }
        if (request.assigneeId() != null) {
            UUID currentAssigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;
            if (!request.assigneeId().equals(currentAssigneeId)) {
                newAssignee = userRepository.findById(request.assigneeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + request.assigneeId()));
                ensureProjectMember(task.getProject(), newAssignee);
                historyEntries.add(buildHistory(task, currentUser, "assigneeId",
                        currentAssigneeId != null ? currentAssigneeId.toString() : null,
                        request.assigneeId().toString()));
                task.setAssignee(newAssignee);
                assigneeChanged = true;
                log.info("Task assigned: taskId={}, assignee={}", taskId, newAssignee.getEmail());
            }
        }
        if (request.dueDate() != null && !request.dueDate().equals(task.getDueDate())) {
            historyEntries.add(buildHistory(task, currentUser, "dueDate",
                    formatDate(task.getDueDate()), formatDate(request.dueDate())));
            task.setDueDate(request.dueDate());
        }
        if (request.estimatedHours() != null && !request.estimatedHours().equals(task.getEstimatedHours())) {
            historyEntries.add(buildHistory(task, currentUser, "estimatedHours",
                    task.getEstimatedHours() != null ? task.getEstimatedHours().toString() : null,
                    request.estimatedHours().toString()));
            task.setEstimatedHours(request.estimatedHours());
        }

        if (!historyEntries.isEmpty()) {
            taskHistoryRepository.saveAll(historyEntries);
        }

        Task updated = taskRepository.save(task);
        log.info("Task updated: id={}", updated.getId());

        if (statusChanged) {
            Hibernate.initialize(updated.getReporter());
            eventPublisher.publishEvent(new TaskStatusChangedEvent(
                    updated, oldStatus, updated.getStatus(), currentUser));
        }
        if (assigneeChanged && newAssignee != null) {
            Hibernate.initialize(updated.getProject());
            eventPublisher.publishEvent(new TaskAssignedEvent(updated, newAssignee, currentUser));
        }

        return taskMapper.toResponse(updated);
    }

    @Transactional
    @Auditable(action = "DELETE_TASK", entityType = "TASK")
    public void deleteTask(UUID taskId) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Task task = findTaskOrThrow(taskId);
        ensureCanDeleteTask(task, currentUser);

        taskRepository.delete(task);
        log.info("Task deleted: id={}", taskId);
    }

    @Transactional
    public TaskCommentResponse addComment(UUID taskId, AddCommentRequest request) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Task task = findTaskOrThrow(taskId);
        ensureMember(task.getProject(), currentUser);

        TaskComment comment = TaskComment.builder()
                .task(task)
                .author(currentUser)
                .content(request.content())
                .build();

        TaskComment saved = taskCommentRepository.save(comment);
        log.info("Comment added: taskId={}, author={}", taskId, currentUser.getEmail());
        Hibernate.initialize(task.getAssignee());
        eventPublisher.publishEvent(new TaskCommentAddedEvent(task, saved, currentUser));
        return taskMapper.toCommentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskCommentResponse> getTaskComments(UUID taskId) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Task task = findTaskOrThrow(taskId);
        ensureMember(task.getProject(), currentUser);

        return taskCommentRepository.findByTaskOrderByCreatedAtAsc(task).stream()
                .map(taskMapper::toCommentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getTaskHistory(UUID taskId) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Task task = findTaskOrThrow(taskId);
        ensureMember(task.getProject(), currentUser);

        return taskHistoryRepository.findByTaskOrderByChangedAtAsc(task).stream()
                .map(taskMapper::toHistoryResponse)
                .toList();
    }

    private Task findTaskOrThrow(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private void ensureMember(Project project, User user) {
        if (project.getOwner().getId().equals(user.getId())) {
            return;
        }
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new UnauthorizedException("You are not a member of this project");
        }
    }

    private void ensureProjectMember(Project project, User user) {
        if (!projectMemberRepository.existsByProjectAndUser(project, user)
                && !project.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("User is not a member of this project");
        }
    }

    private void ensureCanDeleteTask(Task task, User user) {
        if (task.getReporter().getId().equals(user.getId())) {
            return;
        }
        Project project = task.getProject();
        if (project.getOwner().getId().equals(user.getId())) {
            return;
        }
        ProjectMember member = projectMemberRepository.findByProjectAndUser(project, user).orElse(null);
        if (member != null && (member.getRole() == ProjectRole.OWNER || member.getRole() == ProjectRole.MANAGER)) {
            return;
        }
        throw new UnauthorizedException("You are not authorized to delete this task");
    }

    private TaskHistory buildHistory(Task task, User changedBy, String fieldName, String oldValue, String newValue) {
        return TaskHistory.builder()
                .task(task)
                .changedBy(changedBy)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : null;
    }
}
