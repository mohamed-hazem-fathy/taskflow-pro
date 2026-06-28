package com.taskflow.security;

import com.taskflow.domain.entity.User;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static User getCurrentUser(UserRepository userRepository) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}
