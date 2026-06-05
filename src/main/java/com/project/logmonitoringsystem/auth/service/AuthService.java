package com.project.logmonitoringsystem.auth.service;

import com.project.logmonitoringsystem.auth.dto.*;
import com.project.logmonitoringsystem.auth.model.User;
import com.project.logmonitoringsystem.auth.repository.UserRepository;
import com.project.logmonitoringsystem.exception.ResourceNotFoundException;
import com.project.logmonitoringsystem.security.service.JwtService;
import com.project.logmonitoringsystem.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public RegisterResponseDTO registerUser(RegisterRequestDTO request) {
        log.info("REGISTRATION_ATTEMPT username={} email={}", request.username(), request.email());
        try {
            if(userRepository.existsByEmail(request.email())) {
                log.warn("REGISTRATION_FAILED username={} reason=email_already_exists email={}", request.username(), request.email());
                throw new ResourceNotFoundException.BadRequestException("Email already exists");
            }

            if(userRepository.existsByUsername(request.username())) {
                log.warn("REGISTRATION_FAILED username={} reason=username_already_exists", request.username());
                throw new ResourceNotFoundException.BadRequestException("Username already exists");
            }

            User user = User.builder()
                    .username(request.username())
                    .password(passwordEncoder.encode(request.password()))
                    .email(request.email())
                    .role(User.Role.USER)
                    .createdAt(LocalDateTime.now())
                    .build();

            log.info("DB_QUERY executed table=users operation=insert username={}", request.username());
            User saveduser = userRepository.save(user);

            auditLogService.logAction(
                    saveduser.getUsername(),
                    "/api/auth/register",
                    saveduser.getEmail(),
                    "POST"
            );

            log.info("REGISTRATION_SUCCESS userId={} username={} email={}",
                saveduser.getId(), saveduser.getUsername(), saveduser.getEmail());

            return new RegisterResponseDTO(
                    saveduser.getId(),
                    saveduser.getUsername(),
                    saveduser.getEmail(),
                    saveduser.getRole(),
                    "User Registered Successfully"
                    );
        } catch (Exception e) {
            log.error("REGISTRATION_ERROR username={} exception={} reason={}",
                request.username(), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        log.info("LOGIN_ATTEMPT login={}", request.login());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.login(), request.password())
            );
            log.info("AUTHENTICATION_SUCCESS login={}", request.login());

            log.info("DB_QUERY executed table=users operation=select filter=email_or_username login={}", request.login());
            User user = userRepository.findByEmail(request.login())
                    .orElseGet(() -> userRepository.findByUsername(request.login())
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with email or username: " + request.login())));

            log.info("USER_FOUND userId={} username={} role={}", user.getId(), user.getUsername(), user.getRole());

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole().name());

            String accessToken = jwtService.generateAccessToken(user.getEmail(), claims);
            String refreshToken = jwtService.generateRefreshToken(user.getEmail(), claims);

            log.info("TOKENS_GENERATED userId={} accessToken_generated=true refreshToken_generated=true", user.getId());

            auditLogService.logAction(
                    user.getUsername(),
                    "/api/auth/login",
                    user.getEmail(),
                    "POST"
            );

            log.info("LOGIN_SUCCESS userId={} username={} email={}", user.getId(), user.getUsername(), user.getEmail());
            return new LoginResponseDTO(accessToken, refreshToken);
        } catch (Exception e) {
            log.error("LOGIN_FAILED login={} exception={} reason={}",
                request.login(), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        log.info("TOKEN_REFRESH_REQUEST token_type=refresh");
        try {
            String refreshToken = request.refreshToken();

            String tokenType =
                    jwtService.extractTokenType(refreshToken);

            if (!"refresh".equals(tokenType)) {
                log.warn("TOKEN_REFRESH_FAILED reason=invalid_token_type expected=refresh received={}", tokenType);
                throw new ResourceNotFoundException.InvalidTokenException(
                        "Invalid token type");
            }

            String username = jwtService.extractUsername(refreshToken);
            log.info("TOKEN_REFRESH_USERNAME_EXTRACTED username={}", username);

            log.info("DB_QUERY executed table=users operation=select filter=email_or_username username={}", username);
            User user = userRepository
                    .findByEmail(username)
                    .orElseGet(() -> userRepository.findByUsername(username)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with email or username: " + username)));

            if(!jwtService.isTokenValid(refreshToken, user.getEmail())) {
                log.warn("TOKEN_REFRESH_FAILED userId={} reason=invalid_or_expired_token", user.getId());
                throw new RuntimeException("Invalid refresh token");
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole().name());

            String newAccessToken = jwtService.generateAccessToken(user.getEmail(), claims);

            log.info("NEW_ACCESS_TOKEN_GENERATED userId={} username={}", user.getId(), user.getUsername());

            auditLogService.logAction(
                    user.getUsername(),
                    "/api/auth/refresh",
                    user.getEmail(),
                    "POST"
            );

            log.info("TOKEN_REFRESH_SUCCESS userId={} username={}", user.getId(), user.getUsername());
            return new RefreshTokenResponseDTO(newAccessToken);
        } catch (Exception e) {
            log.error("TOKEN_REFRESH_ERROR exception={} reason={}",
                e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    public List<User> getAllUsers() {
        log.info("API_CALL operation=fetch_all_users");
        try {
            log.info("DB_QUERY executed table=users operation=fetch_all");
            List<User> user = userRepository.findAll();
            log.info("USERS_RETRIEVED count={}", user.size());
            return user;
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=users operation=fetch_all exception={}", e.getMessage());
            throw e;
        }
    }
}
