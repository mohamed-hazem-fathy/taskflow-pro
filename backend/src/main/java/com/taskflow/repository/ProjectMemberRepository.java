package com.taskflow.repository;

import com.taskflow.domain.entity.Project;
import com.taskflow.domain.entity.ProjectMember;
import com.taskflow.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    boolean existsByProjectAndUser(Project project, User user);

    List<ProjectMember> findByProject(Project project);
}
