package com.taskflow.dto.request;

import com.taskflow.domain.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberRequest(
        @NotNull UUID userId,
        ProjectRole role
) {
    public AddMemberRequest {
        if (role == null) {
            role = ProjectRole.MEMBER;
        }
    }
}
