package com.project.logmonitoringsystem.auth.dto;

import lombok.Builder;

@Builder
public record RegisterRequestDTO(
        String username,
        String email,
        String password,
        String role
) {}
