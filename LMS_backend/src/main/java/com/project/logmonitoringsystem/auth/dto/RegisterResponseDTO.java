package com.project.logmonitoringsystem.auth.dto;

import com.project.logmonitoringsystem.auth.model.User;

public record RegisterResponseDTO(
        Long id,
        String username,
        String email,
        User.Role role,
        String message
) {}
