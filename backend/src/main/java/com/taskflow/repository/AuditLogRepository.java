package com.taskflow.repository;

import com.taskflow.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByUserIdOrderByOccurredAtDesc(UUID userId, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(String entityType, UUID entityId, Pageable pageable);

    Page<AuditLog> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
