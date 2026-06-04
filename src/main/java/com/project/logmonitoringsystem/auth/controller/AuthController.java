package com.project.logmonitoringsystem.auth.controller;

import com.project.logmonitoringsystem.auth.dto.*;
import com.project.logmonitoringsystem.auth.model.User;
import com.project.logmonitoringsystem.auth.service.AuthService;
import com.project.logmonitoringsystem.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuditLogService auditLogService;

    @Operation(
            summary = "User register"
    )
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        RegisterResponseDTO registerResponseDTO = authService.registerUser(request);
        return ResponseEntity.ok(registerResponseDTO);
    }

    @Operation(
            summary = "User login"
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO loginResponseDTO = authService.login(request);
        return ResponseEntity.ok(loginResponseDTO);
    }

    @Operation(
            summary = "Refresh accessToken",
            description = "Give refreshToken to get accessToken"
    )
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO request) {
        RefreshTokenResponseDTO refreshTokenResponseDTO = authService.refreshToken(request);
        return ResponseEntity.ok(refreshTokenResponseDTO);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }


}
