package com.company.gym.controller;

import com.company.gym.dto.request.AuthenticationRequest;
import com.company.gym.dto.request.ChangePasswordRequest;
import com.company.gym.dto.response.AuthenticationResponse;
import com.company.gym.security.JwtUtil;
import com.company.gym.security.LoginAttemptService;
import com.company.gym.service.AuthenticationService;
import com.company.gym.service.TraineeService;
import com.company.gym.service.TrainerService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.Objects;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthController authController;

    private ChangePasswordRequest changePasswordRequest;
    private AuthenticationRequest authenticationRequest;

    @BeforeEach
    void setUp() {
        authenticationRequest = new AuthenticationRequest("test.user", "password");

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setUsername("test.user");
        changePasswordRequest.setOldPassword("oldPass");
        changePasswordRequest.setNewPassword("newPass");
    }

    @Test
    @DisplayName("Login should return 200 OK with JWT for valid credentials")
    void createAuthenticationToken_validCredentials() {
        UserDetails userDetails = new User("test.user", "password", new ArrayList<>());
        when(loginAttemptService.isBlocked(any())).thenReturn(false);
        when(userDetailsService.loadUserByUsername("test.user")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("dummy.jwt.token");

        ResponseEntity<AuthenticationResponse> response = authController.createAuthenticationToken(authenticationRequest, httpServletRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(Objects.requireNonNull(response.getBody()));
        assertEquals("dummy.jwt.token", response.getBody().getJwt());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("Change Password should return 200 OK when user is a Trainee")
    void changePassword_userIsTrainee() {
        doNothing().when(traineeService).changePassword(anyString(), anyString(), anyString());

        ResponseEntity<Void> response = authController.changePassword(changePasswordRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(traineeService).changePassword(
                eq("test.user"),
                eq("oldPass"),
                eq("newPass")
        );
        verify(trainerService, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Change Password should return 200 OK when user is a Trainer")
    void changePassword_userIsTrainer() {
        doThrow(new SecurityException("Trainee not found"))
                .when(traineeService).changePassword(anyString(), anyString(), anyString());
        doNothing().when(trainerService).changePassword(anyString(), anyString(), anyString());

        ResponseEntity<Void> response = authController.changePassword(changePasswordRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(traineeService).changePassword(
                eq("test.user"),
                eq("oldPass"),
                eq("newPass")
        );
        verify(trainerService).changePassword(
                eq("test.user"),
                eq("oldPass"),
                eq("newPass")
        );
    }
}
