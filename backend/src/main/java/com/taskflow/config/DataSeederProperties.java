package com.taskflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.seed")
@Getter
@Setter
public class DataSeederProperties {

    /**
     * When false, no seeding runs (recommended for integration tests).
     */
    private boolean enabled = true;

    private String adminEmail = "mh5725012@gmail.com";

    private String adminUsername = "admin";

    private String adminFullName = "System Administrator";

    /**
     * BCrypt-encoded password is not supported here — provide a plain password
     * via ADMIN_PASSWORD (or app.seed.admin-password). Seeding is skipped when blank.
     */
    private String adminPassword = "";

    /**
     * When true, seeds sample projects, tasks, and comments if no projects exist yet.
     */
    private boolean demoDataEnabled = true;
}
