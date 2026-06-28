package com.taskflow.service;

import com.taskflow.domain.entity.Project;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.ProjectRole;
import com.taskflow.domain.enums.ProjectStatus;
import com.taskflow.dto.request.AddMemberRequest;
import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.dto.response.UserSummaryResponse;
import com.taskflow.event.ProjectMemberAddedEvent;
import com.taskflow.exception.DuplicateResourceException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.mapper.ProjectMapper;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProjectService projectService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder().email("owner@test.com").username("owner").fullName("Owner").build();
        owner.setId(UUID.randomUUID());
        setSecurityContext(owner);
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
    }

    private void setSecurityContext(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password("password")
                .authorities("ROLE_MANAGER")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    void createProject_success_ownerAutoAddedAsOwnerMember() {
        CreateProjectRequest request = new CreateProjectRequest("New Project", "Desc", null, null);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(UUID.randomUUID());
            project.setCreatedAt(LocalDateTime.now());
            project.setUpdatedAt(LocalDateTime.now());
            return project;
        });
        when(projectMemberRepository.findByProject(any())).thenReturn(List.of());
        when(taskRepository.countByProject(any())).thenReturn(0L);
        when(projectMapper.toResponse(any())).thenReturn(new ProjectResponse(
                UUID.randomUUID(), "New Project", "Desc", ProjectStatus.ACTIVE,
                new UserSummaryResponse(owner.getId(), owner.getFullName(), owner.getUsername(), owner.getEmail(), null),
                null, null, 0, 0, LocalDateTime.now()));

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.name()).isEqualTo("New Project");
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        assertThat(projectCaptor.getValue().getMembers()).hasSize(1);
        assertThat(projectCaptor.getValue().getMembers().get(0).getRole()).isEqualTo(ProjectRole.OWNER);
    }

    @Test
    void addMember_duplicate_throwsDuplicateResourceException() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User userToAdd = User.builder().email("member@test.com").username("member").fullName("Member").build();
        userToAdd.setId(userId);
        Project project = Project.builder().owner(owner).name("Project").build();
        project.setId(projectId);
        AddMemberRequest request = new AddMemberRequest(userId, ProjectRole.MEMBER);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToAdd));
        when(projectMemberRepository.existsByProjectAndUser(project, userToAdd)).thenReturn(true);

        assertThatThrownBy(() -> projectService.addMember(projectId, request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(eventPublisher, never()).publishEvent(any(ProjectMemberAddedEvent.class));
    }

    @Test
    void deleteProject_notOwner_throwsUnauthorizedException() {
        UUID projectId = UUID.randomUUID();
        User projectOwner = User.builder().email("real-owner@test.com").username("realowner").fullName("Owner").build();
        projectOwner.setId(UUID.randomUUID());
        Project project = Project.builder().owner(projectOwner).name("Project").build();
        project.setId(projectId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.deleteProject(projectId))
                .isInstanceOf(UnauthorizedException.class);
        verify(projectRepository, never()).delete(any(Project.class));
    }
}
