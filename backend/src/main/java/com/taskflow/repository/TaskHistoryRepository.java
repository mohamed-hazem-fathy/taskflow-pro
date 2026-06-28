package com.taskflow.repository;

import com.taskflow.domain.entity.Task;
import com.taskflow.domain.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, UUID> {

    List<TaskHistory> findByTaskOrderByChangedAtAsc(Task task);
}
