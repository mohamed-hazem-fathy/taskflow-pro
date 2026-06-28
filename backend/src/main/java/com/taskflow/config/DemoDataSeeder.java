package com.taskflow.config;

import com.taskflow.domain.entity.Project;
import com.taskflow.domain.entity.ProjectMember;
import com.taskflow.domain.entity.Task;
import com.taskflow.domain.entity.TaskComment;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.ProjectRole;
import com.taskflow.domain.enums.ProjectStatus;
import com.taskflow.domain.enums.TaskPriority;
import com.taskflow.domain.enums.TaskStatus;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskCommentRepository;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataSeeder {

    private final DataSeederProperties seedProperties;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;

    public void seedIfEmpty(User owner) {
        if (!seedProperties.isDemoDataEnabled()) {
            log.debug("Demo data seeding is disabled");
            return;
        }

        if (projectRepository.count() > 0) {
            log.debug("Demo data skipped: database already contains projects");
            return;
        }

        if (owner == null) {
            log.warn("Demo data skipped: no admin user available as project owner");
            return;
        }

        Project websiteProject = createProject(
                owner,
                "Website Redesign",
                "Refresh the marketing site with a modern UI and improved performance.",
                ProjectStatus.ACTIVE,
                LocalDate.now().minusWeeks(2),
                LocalDate.now().plusMonths(2)
        );

        Project mobileProject = createProject(
                owner,
                "Mobile App Launch",
                "Ship the first public release of the TaskFlow mobile app.",
                ProjectStatus.PLANNING,
                LocalDate.now().plusWeeks(1),
                LocalDate.now().plusMonths(4)
        );

        Task designTask = createTask(
                websiteProject,
                owner,
                owner,
                "Design homepage mockups",
                "Create wireframes and high-fidelity mockups for the new landing page.",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                LocalDate.now().plusWeeks(1),
                12
        );

        Task authTask = createTask(
                websiteProject,
                owner,
                owner,
                "Implement authentication flow",
                "Add login, registration, and password reset screens.",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                LocalDate.now().plusWeeks(2),
                16
        );

        Task docsTask = createTask(
                websiteProject,
                owner,
                owner,
                "Write API documentation",
                "Document public REST endpoints for partner integrations.",
                TaskStatus.DONE,
                TaskPriority.MEDIUM,
                LocalDate.now().minusDays(3),
                8
        );
        docsTask.setCompletedAt(LocalDateTime.now().minusDays(1));

        Task mvpTask = createTask(
                mobileProject,
                owner,
                owner,
                "Define MVP feature set",
                "Prioritize must-have screens and flows for v1.",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                LocalDate.now().plusWeeks(3),
                6
        );

        Task cicdTask = createTask(
                mobileProject,
                owner,
                owner,
                "Set up CI/CD pipeline",
                "Automate builds, tests, and TestFlight deployment.",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                LocalDate.now().plusWeeks(4),
                10
        );

        taskRepository.saveAll(List.of(designTask, authTask, docsTask, mvpTask, cicdTask));

        createComment(designTask, owner, "Let's align on the color palette before final mockups.");
        createComment(designTask, owner, "Shared Figma link in the project channel.");
        createComment(authTask, owner, "JWT refresh token flow still needs review.");
        createComment(docsTask, owner, "Swagger export looks good — ready for partners.");
        createComment(cicdTask, owner, "GitHub Actions workflow draft is in progress.");

        log.info(
                "Seeded demo data: {} projects, {} tasks, comments on key tasks",
                2,
                5
        );
    }

    private Project createProject(
            User owner,
            String name,
            String description,
            ProjectStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Project project = Project.builder()
                .name(name)
                .description(description)
                .status(status)
                .owner(owner)
                .startDate(startDate)
                .endDate(endDate)
                .members(new ArrayList<>())
                .build();

        ProjectMember ownerMember = ProjectMember.builder()
                .project(project)
                .user(owner)
                .role(ProjectRole.OWNER)
                .build();
        project.getMembers().add(ownerMember);

        return projectRepository.save(project);
    }

    private Task createTask(
            Project project,
            User reporter,
            User assignee,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            Integer estimatedHours
    ) {
        return Task.builder()
                .project(project)
                .reporter(reporter)
                .assignee(assignee)
                .title(title)
                .description(description)
                .status(status)
                .priority(priority)
                .dueDate(dueDate)
                .estimatedHours(estimatedHours)
                .comments(new ArrayList<>())
                .history(new ArrayList<>())
                .build();
    }

    private void createComment(Task task, User author, String content) {
        TaskComment comment = TaskComment.builder()
                .task(task)
                .author(author)
                .content(content)
                .build();
        taskCommentRepository.save(comment);
    }
}
