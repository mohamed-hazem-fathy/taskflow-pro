package com.taskflow.repository;

import com.taskflow.domain.entity.Task;
import com.taskflow.domain.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    List<TaskComment> findByTaskOrderByCreatedAtAsc(Task task);
}
