package com.company.gym.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.company.gym.dao.impl.UserDAOImpl;
import com.company.gym.entity.Credentials;
import com.company.gym.entity.User;
import com.company.gym.exception.InvalidCredentialsException;
import com.company.gym.service.impl.AuthenticationServiceImpl;
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

    @Test
    void authenticate_Success() {
        User user = new User();
        user.setUsername("test.user");
        user.setPassword("correctPassword");

        when(userDAO.findByUsername("test.user")).thenReturn(Optional.of(user));

        Credentials credentials = new Credentials("test.user", "correctPassword");
        assertDoesNotThrow(() -> authenticationService.authenticate(credentials));
    }

    @Test
    void authenticate_UserNotFound() {
        when(userDAO.findByUsername("unknown.user")).thenReturn(Optional.empty());

        Credentials credentials = new Credentials("unknown.user", "anyPassword");
        assertThrows(InvalidCredentialsException.class,
                () -> authenticationService.authenticate(credentials));
    }

    @Test
    void authenticate_WrongPassword() {
        User user = new User();
        user.setUsername("test.user");
        user.setPassword("correctPassword");

        when(userDAO.findByUsername("test.user")).thenReturn(Optional.of(user));

        Credentials credentials = new Credentials("test.user", "wrongPassword");
        assertThrows(InvalidCredentialsException.class,
                () -> authenticationService.authenticate(credentials));
    }
}