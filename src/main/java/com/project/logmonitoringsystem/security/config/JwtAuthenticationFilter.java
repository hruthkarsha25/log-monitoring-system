package com.project.logmonitoringsystem.security.config;

import com.project.logmonitoringsystem.auth.repository.UserRepository;
import com.project.logmonitoringsystem.enums.LogLevel;
import com.project.logmonitoringsystem.security.service.CustomUserDetailsService;
import com.project.logmonitoringsystem.security.service.JwtService;
import com.project.logmonitoringsystem.service.EventLoggingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final EventLoggingService eventLoggingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();

        if (path.startsWith("/auth/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        String username = null;

        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            eventLoggingService.log(
                    "auth-service",
                    LogLevel.WARN,
                    "JWT_INVALID",
                    request.getRequestURI(),
                    request.getMethod(),
                    username
            );
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(token, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                eventLoggingService.log(
                        "auth-service",
                        LogLevel.WARN,
                        "JWT_EXPIRED_OR_INVALID",
                        request.getRequestURI(),
                        request.getMethod(),
                        username
                );
            }
        }
        filterChain.doFilter(request, response);
    }
}
