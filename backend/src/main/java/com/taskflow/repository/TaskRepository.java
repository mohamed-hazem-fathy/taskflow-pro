package com.taskflow.repository;

import com.taskflow.domain.entity.Project;
import com.taskflow.domain.entity.Task;
import com.taskflow.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    Page<Task> findByProject(Project project, Pageable pageable);

    Page<Task> findByAssignee(User assignee, Pageable pageable);

    long countByProject(Project project);
}
