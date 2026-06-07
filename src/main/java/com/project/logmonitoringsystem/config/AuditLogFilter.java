package com.project.logmonitoringsystem.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.logmonitoringsystem.auth.model.User;
import com.project.logmonitoringsystem.auth.repository.UserRepository;
import com.project.logmonitoringsystem.dto.AuditEvent;
import com.project.logmonitoringsystem.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class AuditLogFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, AuditEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    public AuditLogFilter(
            UserRepository userRepository,
            @Qualifier("auditKafkaTemplate") KafkaTemplate<String, AuditEvent> kafkaTemplate,
            ObjectMapper objectMapper,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        CachedBodyHttpServletRequest cachedRequest =
                new CachedBodyHttpServletRequest(request);

        String uri = cachedRequest.getRequestURI();
        String method = cachedRequest.getMethod();

        String username = "ANONYMOUS";
        String email = "ANONYMOUS";

        try {
            String body = new String(cachedRequest.getCachedBody());
            JsonNode json = objectMapper.readTree(body);

            if (uri.contains("/auth/login")) {
                String login = json.has("login") ?
                        json.get("login").asText() : null;

                if (login != null) {
                    User user = userRepository.findByEmail(login)
                            .orElseGet(() -> userRepository.findByUsername(login)
                                    .orElse(null));
                    if (user != null) {
                        username = user.getUsername();
                        email = user.getEmail();
                    } else {
                        username = login;
                        email = login;
                    }
                }

            } else if (uri.contains("/auth/register")) {
                username = json.has("username") ?
                        json.get("username").asText() : "ANONYMOUS";
                email = json.has("email") ?
                        json.get("email").asText() : "ANONYMOUS";

            } else if (uri.contains("/auth/refresh")) {
                String refreshToken = json.has("refreshToken") ?
                        json.get("refreshToken").asText() : null;

                if (refreshToken != null) {
                    String extractedEmail = jwtService.extractUsername(refreshToken);
                    User user = userRepository.findByEmail(extractedEmail)
                            .orElseGet(() -> userRepository.findByUsername(extractedEmail)
                                    .orElse(null));
                    if (user != null) {
                        username = user.getUsername();
                        email = user.getEmail();
                    } else {
                        username = extractedEmail;
                        email = extractedEmail;
                    }
                }

            } else {
                // All other endpoints — JWT already validated, SecurityContext is populated
                Authentication authentication =
                        SecurityContextHolder.getContext().getAuthentication();

                if (authentication != null
                        && authentication.isAuthenticated()
                        && !"anonymousUser".equals(authentication.getName())) {

                    email = authentication.getName();
                    User user = userRepository.findByEmail(email).orElse(null);
                    username = (user != null) ? user.getUsername() : email;
                }
            }
        } catch (Exception e) {
            // leave as ANONYMOUS if anything fails
        }

        filterChain.doFilter(cachedRequest, response);

        AuditEvent event = new AuditEvent(
                username,
                email,
                uri,
                method,
                LocalDateTime.now()
        );

        kafkaTemplate.send("audit-events", event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.err.println("AUDIT EVENT FAILED: " + ex.getMessage());
                    }
                });
    }
}