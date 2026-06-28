package com.taskflow.dto.request;

import com.taskflow.domain.enums.RoleType;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull RoleType role
) {
}
