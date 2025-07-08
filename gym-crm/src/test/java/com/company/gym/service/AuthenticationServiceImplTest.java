package com.company.gym.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.company.gym.dao.impl.UserDAOImpl;
import com.company.gym.entity.Credentials;
import com.company.gym.entity.User;
import com.company.gym.exception.InactiveUserException;
import com.company.gym.exception.InvalidCredentialsException;
import com.company.gym.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserDAOImpl userDAO;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User activeUser;
    private User inactiveUser;
    private Credentials validCredentials;
    private Credentials invalidCredentials;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setUsername("john.doe");
        activeUser.setPassword("password123");
        activeUser.setIsActive(true);

        inactiveUser = new User();
        inactiveUser.setUsername("jane.doe");
        inactiveUser.setPassword("password456");
        inactiveUser.setIsActive(false);

        validCredentials = new Credentials("john.doe", "password123");
        invalidCredentials = new Credentials("unknown.user", "wrongpass");
    }

    @Test
    @DisplayName("Authenticate should succeed with valid credentials")
    void authenticate_success() {
        when(userDAO.findByUsername("john.doe")).thenReturn(Optional.of(activeUser));

        assertDoesNotThrow(() -> authenticationService.authenticate(validCredentials));
        verify(userDAO, times(1)).findByUsername("john.doe");
    }

    @Test
    @DisplayName("Authenticate should throw InvalidCredentialsException for unknown user")
    void authenticate_userNotFound() {
        when(userDAO.findByUsername("unknown.user")).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authenticationService.authenticate(invalidCredentials));

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userDAO, times(1)).findByUsername("unknown.user");
    }

    @Test
    @DisplayName("Authenticate should throw InactiveUserException for inactive user")
    void authenticate_inactiveUser() {
        when(userDAO.findByUsername("jane.doe")).thenReturn(Optional.of(inactiveUser));

        InactiveUserException exception = assertThrows(InactiveUserException.class,
                () -> authenticationService.authenticate(new Credentials("jane.doe", "password456")));

        assertEquals("User is not active", exception.getMessage());
        verify(userDAO, times(1)).findByUsername("jane.doe");
    }

    @Test
    @DisplayName("Authenticate should log warning when user not found")
    void authenticate_logsWarningForInvalidUser() {
        when(userDAO.findByUsername("unknown.user")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authenticationService.authenticate(invalidCredentials));

    }
}
