package com.taskflow.dto.request;

public record UpdateProfileRequest(
        String fullName,
        String avatarUrl
) {
}
