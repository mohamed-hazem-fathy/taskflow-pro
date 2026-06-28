package com.taskflow.service;

import com.taskflow.domain.entity.Role;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.RoleType;
import com.taskflow.dto.request.UpdateProfileRequest;
import com.taskflow.dto.request.UpdateUserRoleRequest;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.mapper.UserMapper;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return userMapper.toResponse(findUserOrThrow(id));
    }

    @Transactional
    public UserResponse updateUserRole(UUID id, UpdateUserRoleRequest request) {
        User user = findUserOrThrow(id);
        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.role()));

        user.setRoles(Set.of(role));
        User saved = userRepository.save(user);
        log.info("User role updated: userId={}, role={}", id, request.role());
        return userMapper.toResponse(saved);
    }

    @Transactional
    public void deactivateUser(UUID id) {
        User user = findUserOrThrow(id);
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: userId={}", id);
    }

    @Transactional
    public void activateUser(UUID id) {
        User user = findUserOrThrow(id);
        user.setActive(true);
        userRepository.save(user);
        log.info("User activated: userId={}", id);
    }

    @Transactional(readOnly = true)
    public UserResponse getMyProfile() {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        return userMapper.toResponse(currentUser);
    }

    @Transactional
    public UserResponse updateMyProfile(UpdateProfileRequest request) {
        User currentUser = SecurityUtils.getCurrentUser(userRepository);

        if (request.fullName() != null) {
            currentUser.setFullName(request.fullName());
        }
        if (request.avatarUrl() != null) {
            currentUser.setAvatarUrl(request.avatarUrl());
        }

        User saved = userRepository.save(currentUser);
        log.info("Profile updated: userId={}", saved.getId());
        return userMapper.toResponse(saved);
    }

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
