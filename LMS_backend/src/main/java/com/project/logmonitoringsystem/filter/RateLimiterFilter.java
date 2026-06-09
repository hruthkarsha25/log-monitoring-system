package com.project.logmonitoringsystem.filter;

import com.project.logmonitoringsystem.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimiterFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String userKey = getUserKey(request);
            boolean allowed = rateLimiterService.isAllowed(userKey);

            if (!allowed) {
                response.setStatus(429);
                response.getWriter().write("Too Many Requests - Rate limit exceeded");
                return;
            }
        } catch (Exception e) {
            log.warn("Rate limiter service unavailable (Redis connection failed). Allowing request. Error: {}", e.getMessage());
            // If Redis is down, allow the request to proceed (graceful degradation)
        }


        filterChain.doFilter(request, response);

    }

    private String getUserKey(HttpServletRequest request) {
        String user = request.getHeader("X-User");

        if(user != null) return user;

        return request.getRemoteAddr();
    }
}
