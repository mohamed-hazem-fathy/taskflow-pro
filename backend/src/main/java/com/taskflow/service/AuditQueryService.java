package com.taskflow.service;

import com.taskflow.domain.entity.AuditLog;
import com.taskflow.dto.response.AuditLogResponse;
import com.taskflow.mapper.AuditLogMapper;
import com.taskflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByOccurredAtDesc(pageable)
                .map(auditLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getByUser(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByOccurredAtDesc(userId, pageable)
                .map(auditLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getByEntity(String entityType, UUID entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(entityType, entityId, pageable)
                .map(auditLogMapper::toResponse);
    }
}
