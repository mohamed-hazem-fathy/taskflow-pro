package com.taskflow.dto.response;

import com.taskflow.domain.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        boolean isRead,
        UUID referenceId,
        String referenceType,
        LocalDateTime createdAt
) {
}
