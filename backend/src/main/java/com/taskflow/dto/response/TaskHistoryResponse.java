package com.taskflow.dto.response;

import java.time.LocalDateTime;

public record TaskHistoryResponse(
        String fieldName,
        String oldValue,
        String newValue,
        UserSummaryResponse changedBy,
        LocalDateTime changedAt
) {
}
