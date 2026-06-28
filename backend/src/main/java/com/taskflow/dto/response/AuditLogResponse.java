package com.taskflow.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID userId,
        String action,
        String entityType,
        UUID entityId,
        String payload,
        String ipAddress,
        LocalDateTime occurredAt
) {
}
