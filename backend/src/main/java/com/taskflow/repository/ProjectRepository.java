package com.taskflow.repository;

import com.taskflow.domain.entity.Project;
import com.taskflow.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    Page<Project> findByOwner(User owner, Pageable pageable);

    boolean existsByIdAndOwner(UUID id, User owner);

    @Query("""
            SELECT DISTINCT p FROM Project p
            LEFT JOIN p.members m
            WHERE p.owner = :user OR m.user = :user
            """)
    Page<Project> findAccessibleByUser(@Param("user") User user, Pageable pageable);
}
