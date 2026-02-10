package com.letsplay.auth;

import com.letsplay.auth.dto.*;
import com.letsplay.common.exception.ConflictException;
import com.letsplay.security.JwtService;
import com.letsplay.user.User;
import com.letsplay.user.UserRepository;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder,
                       AuthenticationManager authManager, JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) throw new ConflictException("Email already in use");

        User u = User.builder()
                .name(req.name())
                .email(req.email().toLowerCase())
                .passwordHash(encoder.encode(req.password()))
                .role(User.Role.ROLE_USER)
                .build();

        userRepo.save(u);
    }

    public AuthResponse login(LoginRequest req) {
        var token = new UsernamePasswordAuthenticationToken(req.email().toLowerCase(), req.password());
        authManager.authenticate(token);

        User u = userRepo.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String jwt = jwtService.generateToken(u.getId(), u.getEmail(), u.getRole().name());
        return new AuthResponse(jwt);
    }
}
