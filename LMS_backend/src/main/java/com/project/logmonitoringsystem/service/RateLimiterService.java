package com.project.logmonitoringsystem.service;

import com.project.logmonitoringsystem.enums.LogLevel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);
    private final EventLoggingService eventLoggingService;

    private static final int MAX_TOKENS = 10;
    private static final int REFILL_RATE = 1;
    public boolean isAllowed(String key) {
        log.info("RATE_LIMIT_CHECK user_key={}", key);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = null;

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getName())) {

            username = authentication.getName();
        }

        try {
            String tokenKey = "bucket:" + key + ":tokens";
            String timeKey = "bucket:" + key + ":last_refill";

            long now = System.currentTimeMillis() / 1000;

            String lastRefillStr = redisTemplate.opsForValue().get(timeKey);
            String tokensStr = redisTemplate.opsForValue().get(tokenKey);

            int tokens;
            long lastRefill;

            if (tokensStr == null || lastRefillStr == null) {
                tokens = MAX_TOKENS;
                lastRefill = now;
                log.info("RATE_LIMIT_BUCKET_CREATED user_key={} tokens={}", key, MAX_TOKENS);
            } else {
                tokens = Integer.parseInt(tokensStr);
                lastRefill = Long.parseLong(lastRefillStr);
            }

            long secondsPassed = now - lastRefill;
            int refillTokens = (int) (secondsPassed * REFILL_RATE);

            if (refillTokens > 0) {
                tokens = Math.min(MAX_TOKENS, tokens + refillTokens);
                lastRefill = now;
                log.info("RATE_LIMIT_REFILL user_key={} refilled_tokens={} total_tokens={}", key, refillTokens, tokens);
            }

            log.info("RATE_LIMIT_STATUS user_key={} available_tokens={}", key, tokens);

            if (tokens <= 0) {
                eventLoggingService.log(
                        "rate-limiter",
                        LogLevel.WARN,
                        "RATE_LIMIT_EXCEEDED",
                        null,
                        null,
                        username

                );
                log.warn("RATE_LIMIT_EXCEEDED user_key={} reason=no_tokens_available", key);
                return false;
            }

            tokens--;

            redisTemplate.opsForValue().set(tokenKey, String.valueOf(tokens));
            redisTemplate.opsForValue().set(timeKey, String.valueOf(lastRefill));

            eventLoggingService.log(
                    "rate-limiter",
                    LogLevel.INFO,
                    "RATE_LIMIT_ALLOWED",
                    null,
                    null,
                    username
            );
            log.info("RATE_LIMIT_ALLOWED user_key={} remaining_tokens={}", key, tokens);
            return true;
        } catch (Exception e) {
            eventLoggingService.log(
                    "rate-limiter",
                    LogLevel.ERROR,
                    "RATE_LIMIT_ERROR",
                    null,
                    null,
                    username
            );
            log.error("RATE_LIMIT_ERROR user_key={} exception={}", key, e.getMessage());
            throw e;
        }
    }
}