package com.taskflow.service;

import com.taskflow.annotation.Auditable;
import com.taskflow.domain.entity.Project;
import com.taskflow.domain.entity.ProjectMember;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.ProjectRole;
import com.taskflow.domain.enums.ProjectStatus;
import com.taskflow.dto.request.AddMemberRequest;
import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.request.UpdateProjectRequest;
import com.taskflow.dto.response.ProjectMemberResponse;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.event.ProjectMemberAddedEvent;
import com.taskflow.exception.DuplicateResourceException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.mapper.ProjectMapper;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Auditable(action = "CREATE_PROJECT", entityType = "PROJECT")
    public ProjectResponse createProject(CreateProjectRequest request) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .status(ProjectStatus.ACTIVE)
                .owner(currentUser)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        ProjectMember ownerMember = ProjectMember.builder()
                .project(project)
                .user(currentUser)
                .role(ProjectRole.OWNER)
                .build();
        project.getMembers().add(ownerMember);

        Project saved = projectRepository.save(project);
        log.info("Project created: id={}, name={}, owner={}", saved.getId(), saved.getName(), currentUser.getEmail());

        return toProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getMyProjects(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        return projectRepository.findAccessibleByUser(currentUser, pageable)
                .map(this::toProjectResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Project project = findProjectOrThrow(id);
        ensureMember(project, currentUser);
        return toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Project project = findProjectOrThrow(id);
        ensureCanManage(project, currentUser);

        if (request.name() != null) {
            project.setName(request.name());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        if (request.startDate() != null) {
            project.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            project.setEndDate(request.endDate());
        }

        Project updated = projectRepository.save(project);
        log.info("Project updated: id={}", updated.getId());
        return toProjectResponse(updated);
    }

    @Transactional
    @Auditable(action = "DELETE_PROJECT", entityType = "PROJECT")
    public void deleteProject(UUID id) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Project project = findProjectOrThrow(id);
        ensureOwner(project, currentUser);

        projectRepository.delete(project);
        log.info("Project deleted: id={}", id);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(UUID id) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Project project = findProjectOrThrow(id);
        ensureMember(project, currentUser);

        return projectMemberRepository.findByProject(project).stream()
                .map(projectMapper::toMemberResponse)
                .toList();
    }

    @Transactional
    @Auditable(action = "ADD_PROJECT_MEMBER", entityType = "PROJECT")
    public ProjectMemberResponse addMember(UUID projectId, AddMemberRequest request) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Project project = findProjectOrThrow(projectId);
        ensureCanManage(project, currentUser);

        User userToAdd = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        if (projectMemberRepository.existsByProjectAndUser(project, userToAdd)) {
            throw new DuplicateResourceException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(userToAdd)
                .role(request.role())
                .build();

        ProjectMember saved = projectMemberRepository.save(member);
        log.info("Member added to project: projectId={}, userId={}, role={}", projectId, request.userId(), request.role());
        Hibernate.initialize(project);
        eventPublisher.publishEvent(new ProjectMemberAddedEvent(project, userToAdd, currentUser));
        return projectMapper.toMemberResponse(saved);
    }

    @Transactional
    @Auditable(action = "REMOVE_PROJECT_MEMBER", entityType = "PROJECT")
    public void removeMember(UUID projectId, UUID userId) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        Project project = findProjectOrThrow(projectId);
        ensureCanManage(project, currentUser);

        if (project.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("Cannot remove the project owner");
        }

        ProjectMember member = projectMemberRepository.findByProjectAndUser(project,
                        userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId)))
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this project"));

        projectMemberRepository.delete(member);
        log.info("Member removed from project: projectId={}, userId={}", projectId, userId);
    }

    private Project findProjectOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private void ensureMember(Project project, User user) {
        if (project.getOwner().getId().equals(user.getId())) {
            return;
        }
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new UnauthorizedException("You are not a member of this project");
        }
    }

    private void ensureCanManage(Project project, User user) {
        if (project.getOwner().getId().equals(user.getId())) {
            return;
        }
        ProjectMember member = projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this project"));

        if (member.getRole() != ProjectRole.OWNER && member.getRole() != ProjectRole.MANAGER) {
            throw new UnauthorizedException("Only project owners and managers can perform this action");
        }
    }

    private void ensureOwner(Project project, User user) {
        if (!project.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the project owner can perform this action");
        }
    }

    private ProjectResponse toProjectResponse(Project project) {
        ProjectResponse mapped = projectMapper.toResponse(project);
        int memberCount = projectMemberRepository.findByProject(project).size();
        int taskCount = (int) taskRepository.countByProject(project);
        return new ProjectResponse(
                mapped.id(),
                mapped.name(),
                mapped.description(),
                mapped.status(),
                mapped.owner(),
                mapped.startDate(),
                mapped.endDate(),
                memberCount,
                taskCount,
                mapped.createdAt()
        );
    }
}
