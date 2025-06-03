package com.company.gym.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private PasswordGenerator passwordGenerator;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGenerator();
    }

    @RepeatedTest(10)
    void generatePassword() {
        String password = passwordGenerator.generatePassword();

        assertEquals(10, password.length());

        for (char c : password.toCharArray()) {
            assertTrue(CHARACTERS.contains(String.valueOf(c)),
                    "Password contains invalid character: " + c);
        }
    }

    @Test
    void generatePassword_Uniqueness() {
        String password1 = passwordGenerator.generatePassword();
        String password2 = passwordGenerator.generatePassword();
        assertNotEquals(password1, password2);
    }

    @Test
    void generatePassword_ContainsAllCharacterTypes() {
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (int i = 0; i < 100; i++) {
            String password = passwordGenerator.generatePassword();

            for (char c : password.toCharArray()) {
                if (Character.isUpperCase(c)) hasUpper = true;
                if (Character.isLowerCase(c)) hasLower = true;
                if (Character.isDigit(c)) hasDigit = true;
            }

            if (hasUpper && hasLower && hasDigit) break;
        }

        assertTrue(hasUpper, "Password should contain uppercase letters");
        assertTrue(hasLower, "Password should contain lowercase letters");
        assertTrue(hasDigit, "Password should contain digits");
    }
}