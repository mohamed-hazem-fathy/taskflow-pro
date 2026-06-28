package com.taskflow.service;

import com.taskflow.domain.entity.Project;
import com.taskflow.domain.entity.Task;
import com.taskflow.domain.entity.TaskHistory;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.TaskPriority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.dto.request.CreateTaskRequest;
import com.taskflow.dto.request.UpdateTaskRequest;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.dto.response.UserSummaryResponse;
import com.taskflow.event.TaskAssignedEvent;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskCommentRepository;
import com.taskflow.repository.TaskHistoryRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().email("user@test.com").username("user").fullName("User").build();
        currentUser.setId(UUID.randomUUID());
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(currentUser.getEmail())
                .password("password")
                .authorities("ROLE_USER")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
    }

    @Test
    void createTask_success_publishesTaskAssignedEventWhenAssigneeProvided() {
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        User assignee = User.builder().email("assignee@test.com").username("assignee").fullName("Assignee").build();
        assignee.setId(assigneeId);
        Project project = Project.builder().name("Project").owner(currentUser).build();
        project.setId(projectId);
        CreateTaskRequest request = new CreateTaskRequest("Task", "Desc", TaskPriority.HIGH, null, projectId, assigneeId, null, null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));
        when(projectMemberRepository.existsByProjectAndUser(any(), any())).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(UUID.randomUUID());
            return task;
        });
        when(taskMapper.toResponse(any())).thenReturn(new TaskResponse(
                UUID.randomUUID(), "Task", "Desc", TaskStatus.TODO, TaskPriority.HIGH,
                new UserSummaryResponse(assigneeId, "Assignee", "assignee", "assignee@test.com", null),
                new UserSummaryResponse(currentUser.getId(), "User", "user", "user@test.com", null),
                null, null, LocalDateTime.now(), LocalDateTime.now()));

        taskService.createTask(request);

        verify(eventPublisher).publishEvent(any(TaskAssignedEvent.class));
    }

    @Test
    void createTask_projectNotFound_throwsResourceNotFoundException() {
        UUID projectId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest("Task", "Desc", TaskPriority.MEDIUM, null, projectId, null, null, null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    void updateTask_statusChange_createsTaskHistory() {
        UUID taskId = UUID.randomUUID();
        Project project = Project.builder().name("Project").owner(currentUser).build();
        project.setId(UUID.randomUUID());
        Task task = Task.builder()
                .title("Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .project(project)
                .reporter(currentUser)
                .build();
        task.setId(taskId);
        UpdateTaskRequest request = new UpdateTaskRequest(null, null, TaskStatus.IN_PROGRESS, null, null, null, null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any())).thenReturn(new TaskResponse(
                taskId, "Task", null, TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM,
                null, new UserSummaryResponse(currentUser.getId(), "User", "user", "user@test.com", null),
                null, null, LocalDateTime.now(), LocalDateTime.now()));

        taskService.updateTask(taskId, request);

        ArgumentCaptor<List<TaskHistory>> historyCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskHistoryRepository).saveAll(historyCaptor.capture());
        assertThat(historyCaptor.getValue()).hasSize(1);
        assertThat(historyCaptor.getValue().get(0).getFieldName()).isEqualTo("status");
    }

    @Test
    void updateTask_assigneeChange_publishesTaskAssignedEvent() {
        UUID taskId = UUID.randomUUID();
        UUID newAssigneeId = UUID.randomUUID();
        User newAssignee = User.builder().email("new@test.com").username("newuser").fullName("New").build();
        newAssignee.setId(newAssigneeId);
        Project project = Project.builder().name("Project").owner(currentUser).build();
        project.setId(UUID.randomUUID());
        Task task = Task.builder()
                .title("Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .project(project)
                .reporter(currentUser)
                .assignee(null)
                .build();
        task.setId(taskId);
        UpdateTaskRequest request = new UpdateTaskRequest(null, null, null, null, newAssigneeId, null, null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectMemberRepository.existsByProjectAndUser(any(), any())).thenReturn(true);
        when(userRepository.findById(newAssigneeId)).thenReturn(Optional.of(newAssignee));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any())).thenReturn(new TaskResponse(
                taskId, "Task", null, TaskStatus.TODO, TaskPriority.MEDIUM,
                new UserSummaryResponse(newAssigneeId, "New", "newuser", "new@test.com", null),
                new UserSummaryResponse(currentUser.getId(), "User", "user", "user@test.com", null),
                null, null, LocalDateTime.now(), LocalDateTime.now()));

        taskService.updateTask(taskId, request);

        verify(eventPublisher).publishEvent(any(TaskAssignedEvent.class));
    }
}
