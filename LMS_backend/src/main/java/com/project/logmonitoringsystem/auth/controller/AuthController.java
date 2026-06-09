package com.project.logmonitoringsystem.auth.controller;

import com.project.logmonitoringsystem.auth.dto.*;
import com.project.logmonitoringsystem.auth.model.User;
import com.project.logmonitoringsystem.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Operation(
            summary = "User register"
    )
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.info("API_LOG endpoint=/auth/register method=POST");
        try {
            RegisterResponseDTO registerResponseDTO = authService.registerUser(request);
            log.info("REGISTRATION_SUCCESS username={} email={} userId={}",
                registerResponseDTO.username(), registerResponseDTO.email(), registerResponseDTO.id());
            return ResponseEntity.ok(registerResponseDTO);
        } catch (Exception e) {
            log.error("REGISTRATION_FAILED username={} exception={} reason={}",
                request.username(), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "User login"
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        log.info("API_LOG endpoint=/auth/login method=POST login={}", request.login());
        try {
            LoginResponseDTO loginResponseDTO = authService.login(request);
            log.info("LOGIN_SUCCESS login={} status=AUTHENTICATED", request.login());
            return ResponseEntity.ok(loginResponseDTO);
        } catch (Exception e) {
            log.error("LOGIN_FAILED login={} exception={} reason={}",
                request.login(), e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Refresh accessToken",
            description = "Give refreshToken to get accessToken"
    )
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO request) {
        log.info("API_LOG endpoint=/auth/refresh method=POST");
        try {
            RefreshTokenResponseDTO refreshTokenResponseDTO = authService.refreshToken(request);
            log.info("TOKEN_REFRESH_SUCCESS status=NEW_ACCESS_TOKEN_GENERATED");
            return ResponseEntity.ok(refreshTokenResponseDTO);
        } catch (Exception e) {
            log.error("TOKEN_REFRESH_FAILED exception={} reason={}",
                e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("API_LOG endpoint=/auth/users method=GET");
        try {
            List<User> users = authService.getAllUsers();
            log.info("DB_QUERY executed table=users operation=fetch_all count={}", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("DB_QUERY_FAILED table=users operation=fetch_all exception={}", e.getMessage());
            throw e;
        }
    }


}
