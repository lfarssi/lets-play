package com.letsplay.config;

import com.letsplay.user.User;
import com.letsplay.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin.enabled:true}")
    private boolean enabled;

    @Value("${seed.admin.name}")
    private String adminName;

    @Value("${seed.admin.email}")
    private String adminEmail;

    @Value("${seed.admin.password}")
    private String adminPassword;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!enabled) return;

        String email = adminEmail.trim().toLowerCase();

        // If user exists, enforce admin role (don’t create duplicates)
        userRepository.findByEmail(email).ifPresentOrElse(existing -> {
            if (existing.getRole() != User.Role.ROLE_ADMIN) {
                existing.setRole(User.Role.ROLE_ADMIN);
                userRepository.save(existing);
            }
        }, () -> {
            User admin = User.builder()
                    .name(adminName)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role(User.Role.ROLE_ADMIN)
                    .build();

            userRepository.save(admin);
        });
    }
}
