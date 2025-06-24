package com.company.gym.util;

import com.company.gym.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generateUsername() {
        when(userService.usernameExists("John.Smith")).thenReturn(false);

        String username = usernameGenerator.generateUsername("John", "Smith");
        assertEquals("John.Smith", username);
    }

    @Test
    void generateUsername_WithDuplicate() {
        when(userService.usernameExists("John.Smith")).thenReturn(true);
        when(userService.usernameExists("John.Smith1")).thenReturn(false);

        String username = usernameGenerator.generateUsername("John", "Smith");
        assertEquals("John.Smith1", username);
    }

    @Test
    void generateUsername_WithMultipleDuplicates() {
        when(userService.usernameExists("John.Smith")).thenReturn(true);
        when(userService.usernameExists("John.Smith1")).thenReturn(true);
        when(userService.usernameExists("John.Smith2")).thenReturn(true);
        when(userService.usernameExists("John.Smith3")).thenReturn(false);

        String username = usernameGenerator.generateUsername("John", "Smith");
        assertEquals("John.Smith3", username);
    }

    @Test
    void generateUsername_WithEmptyNames() {
        when(userService.usernameExists(".")).thenReturn(false);

        String username = usernameGenerator.generateUsername("", "");
        assertEquals(".", username);
    }
}
