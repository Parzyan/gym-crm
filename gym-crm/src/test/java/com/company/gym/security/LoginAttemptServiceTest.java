package com.company.gym.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;
    private final String TEST_IP = "127.0.0.1";

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    @DisplayName("isBlocked should be false after one failed attempt")
    void isBlocked_AfterOneFailure() {
        loginAttemptService.loginFailed(TEST_IP);

        assertFalse(loginAttemptService.isBlocked(TEST_IP), "IP should not be blocked after one attempt.");
    }

    @Test
    @DisplayName("isBlocked should be false after two failed attempts")
    void isBlocked_AfterTwoFailures() {
        loginAttemptService.loginFailed(TEST_IP);
        loginAttemptService.loginFailed(TEST_IP);

        assertFalse(loginAttemptService.isBlocked(TEST_IP), "IP should not be blocked after two attempts.");
    }

    @Test
    @DisplayName("isBlocked should be true after three failed attempts")
    void isBlocked_AfterThreeFailures() {
        loginAttemptService.loginFailed(TEST_IP);
        loginAttemptService.loginFailed(TEST_IP);
        loginAttemptService.loginFailed(TEST_IP);

        assertTrue(loginAttemptService.isBlocked(TEST_IP), "IP should be blocked after three attempts.");
    }

    @Test
    @DisplayName("isBlocked should become false after a successful login")
    void isBlocked_AfterSuccess() {
        loginAttemptService.loginFailed(TEST_IP);
        loginAttemptService.loginFailed(TEST_IP);
        loginAttemptService.loginFailed(TEST_IP);
        assertTrue(loginAttemptService.isBlocked(TEST_IP), "Precondition failed: IP was not blocked.");

        loginAttemptService.loginSucceeded(TEST_IP);

        assertFalse(loginAttemptService.isBlocked(TEST_IP), "IP should be unblocked after a successful login.");
    }
}
