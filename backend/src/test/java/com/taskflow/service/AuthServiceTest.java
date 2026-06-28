package com.taskflow.service;

import com.taskflow.domain.entity.Role;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.RoleType;
import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.exception.DuplicateResourceException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("Test User", "testuser", "user@test.com", "password123");
        Role userRole = Role.builder().id(UUID.randomUUID()).name(RoleType.USER).build();
        User savedUser = User.builder()
                .email(request.email())
                .username(request.username())
                .passwordHash("hashed")
                .fullName(request.fullName())
                .roles(Set.of(userRole))
                .build();
        savedUser.setId(UUID.randomUUID());

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("user@test.com");
        assertThat(response.fullName()).isEqualTo("Test User");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest("Test User", "testuser", "user@test.com", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("user@test.com", "password123");
        Role userRole = Role.builder().id(UUID.randomUUID()).name(RoleType.USER).build();
        User user = User.builder()
                .email(request.email())
                .username("testuser")
                .passwordHash("hashed")
                .fullName("Test User")
                .roles(Set.of(userRole))
                .build();
        user.setId(UUID.randomUUID());
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(request.email())
                .password("hashed")
                .authorities("ROLE_USER")
                .build();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("user@test.com");
    }

    @Test
    void login_invalidCredentials_throwsUnauthorizedException() {
        LoginRequest request = new LoginRequest("user@test.com", "wrong");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }
}
