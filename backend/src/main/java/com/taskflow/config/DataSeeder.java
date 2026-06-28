package com.taskflow.config;

import com.taskflow.domain.entity.Role;
import com.taskflow.domain.entity.User;
import com.taskflow.domain.enums.RoleType;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private final DataSeederProperties seedProperties;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DemoDataSeeder demoDataSeeder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureRoles();
        ensureAdminUser();
        seedDemoDataIfEmpty();
    }

    private void seedDemoDataIfEmpty() {
        String email = seedProperties.getAdminEmail();
        if (!StringUtils.hasText(email)) {
            return;
        }
        userRepository.findByEmail(email).ifPresent(demoDataSeeder::seedIfEmpty);
    }

    private void ensureRoles() {
        Arrays.stream(RoleType.values()).forEach(this::ensureRole);
    }

    private void ensureRole(RoleType roleType) {
        roleRepository.findByName(roleType).orElseGet(() -> {
            Role role = Role.builder().name(roleType).build();
            Role saved = roleRepository.save(role);
            log.info("Seeded missing role: {}", roleType);
            return saved;
        });
    }

    private void ensureAdminUser() {
        String email = seedProperties.getAdminEmail();
        if (!StringUtils.hasText(email)) {
            log.warn("Admin seed skipped: app.seed.admin-email is not set");
            return;
        }

        Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found after seeding"));

        userRepository.findByEmail(email).ifPresentOrElse(
                user -> promoteToAdminIfNeeded(user, adminRole),
                () -> createAdminUser(email, adminRole)
        );
    }

    private void promoteToAdminIfNeeded(User user, Role adminRole) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ADMIN);
        if (isAdmin) {
            log.debug("Admin user already exists: {}", user.getEmail());
            return;
        }

        user.setRoles(Set.of(adminRole));
        userRepository.save(user);
        log.info("Promoted existing user to ADMIN: {}", user.getEmail());
    }

    private void createAdminUser(String email, Role adminRole) {
        String password = seedProperties.getAdminPassword();
        if (!StringUtils.hasText(password)) {
            log.warn(
                    "Admin user '{}' not created: set ADMIN_PASSWORD (or app.seed.admin-password)",
                    email
            );
            return;
        }

        String username = seedProperties.getAdminUsername();
        if (userRepository.existsByUsername(username)) {
            log.warn("Admin seed skipped: username '{}' is already taken", username);
            return;
        }

        User admin = User.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(seedProperties.getAdminFullName())
                .isActive(true)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Created default admin user: {}", email);
    }
}
