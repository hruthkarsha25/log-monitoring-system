package com.project.logmonitoringsystem.auth.service;

import com.project.logmonitoringsystem.auth.dto.*;
import com.project.logmonitoringsystem.auth.model.User;
import com.project.logmonitoringsystem.auth.repository.UserRepository;
import com.project.logmonitoringsystem.exception.ResourceNotFoundException;
import com.project.logmonitoringsystem.security.service.JwtService;
import com.project.logmonitoringsystem.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    public RegisterResponseDTO registerUser(RegisterRequestDTO request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new ResourceNotFoundException.BadRequestException("Email already exists");
        }

        if(userRepository.existsByUsername(request.username())) {
            throw new ResourceNotFoundException.BadRequestException("Username already exists");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .role(User.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        User saveduser = userRepository.save(user);

        auditLogService.logAction(
                saveduser.getUsername(),
                "/api/auth/register",
                saveduser.getEmail(),
                "POST"
        );

        return new RegisterResponseDTO(
                saveduser.getId(),
                saveduser.getUsername(),
                saveduser.getEmail(),
                saveduser.getRole(),
                "User Registered Successfully"
                );
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password())
        );

        User user = userRepository.findByEmail(request.login())
                .orElseGet(() -> userRepository.findByUsername(request.login())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with email or username: " + request.login())));
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());

        String accessToken = jwtService.generateAccessToken(user.getEmail(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), claims);

        auditLogService.logAction(
                user.getUsername(),
                "/api/auth/login",
                user.getEmail(),
                "POST"
        );

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {

        String refreshToken = request.refreshToken();

        String tokenType =
                jwtService.extractTokenType(refreshToken);

        if (!"refresh".equals(tokenType)) {
            throw new ResourceNotFoundException.InvalidTokenException(
                    "Invalid token type");
        }

        String username = jwtService.extractUsername(refreshToken);

        User user = userRepository
                .findByEmail(username)
                .orElseGet(() -> userRepository.findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with email or username: " + username)));

        if(!jwtService.isTokenValid(refreshToken, user.getEmail())) {
            throw new RuntimeException("Invalid refresh token");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());

        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), claims);

        auditLogService.logAction(
                user.getUsername(),
                "/api/auth/refresh",
                user.getEmail(),
                "POST"
        );

        return new RefreshTokenResponseDTO(newAccessToken);
    }

    public List<User> getAllUsers() {

        List<User> user = userRepository.findAll();
        return user;
    }
}
