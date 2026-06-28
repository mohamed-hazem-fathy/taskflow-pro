package com.taskflow.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskCommentResponse(
        UUID id,
        UserSummaryResponse author,
        String content,
        LocalDateTime createdAt
) {
}
