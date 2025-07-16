package com.company.gym.security;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
public class LoginAttemptService {
    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);
    private final int MAX_ATTEMPTS = 3;
    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(key -> 0);
    }

    public void loginFailed(String key) {
        int attempts = attemptsCache.get(key);
        attempts++;
        logger.info("Failed login attempt for IP: {}. Attempt count is now: {}", key, attempts);
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            boolean blocked = attemptsCache.get(key) >= MAX_ATTEMPTS;
            if (blocked) {
                logger.warn("IP address {} is blocked due to too many failed login attempts.", key);
            }
            return blocked;
        } catch (Exception e) {
            return false;
        }
    }

    public void loginSucceeded(String key) {
        logger.info("Successful login for IP: {}. Resetting failed attempts count.", key);
        attemptsCache.invalidate(key);
    }
}
