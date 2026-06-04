package com.project.logmonitoringsystem.config;

import com.project.logmonitoringsystem.auth.model.User;
import com.project.logmonitoringsystem.auth.repository.UserRepository;
import com.project.logmonitoringsystem.model.AuditLog;
import com.project.logmonitoringsystem.repository.AuditLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuditLogFilter extends OncePerRequestFilter {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (uri.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = "ANONYMOUS";
        String email = "ANONYMOUS";

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElse(null);

            username = (user != null) ? user.getUsername() : "ANONYMOUS";
        }

        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .email(email)
                .endpoint(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }
}