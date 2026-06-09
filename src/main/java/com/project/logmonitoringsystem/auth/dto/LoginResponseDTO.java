package com.project.logmonitoringsystem.auth.dto;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        Long id,
        String username,
        String email,
        String role
) {
}
