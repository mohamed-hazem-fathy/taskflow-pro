package com.taskflow.service;

import com.taskflow.domain.entity.AuditLog;
import com.taskflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async("taskExecutor")
    public void saveAsync(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }
}
