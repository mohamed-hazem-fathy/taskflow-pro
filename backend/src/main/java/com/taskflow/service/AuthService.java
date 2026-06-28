package com.taskflow.service;

import com.taskflow.annotation.Auditable;
import com.taskflow.domain.entity.Role;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.RoleType;
import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.exception.DuplicateResourceException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    @Auditable(action = "USER_REGISTERED", entityType = "USER")
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already registered: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username is already taken: " + request.username());
        }

        Role userRole = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default USER role not found"));

        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .isActive(true)
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(savedUser.getEmail())
                .password(savedUser.getPasswordHash())
                .authorities("ROLE_USER")
                .build();

        String token = jwtTokenProvider.generateToken(userDetails);

        return buildAuthResponse(token, savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.email()));

            log.info("User logged in successfully: {}", user.getEmail());
            return buildAuthResponse(token, user);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getFullName(), roles);
    }
}
