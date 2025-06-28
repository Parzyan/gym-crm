package com.company.gym.controller;

import com.company.gym.dto.request.ChangePasswordRequest;
import com.company.gym.entity.Credentials;
import com.company.gym.exception.InvalidCredentialsException;
import com.company.gym.service.AuthenticationService;
import com.company.gym.service.TraineeService;
import com.company.gym.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private AuthController authController;

    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setUsername("test.user");
        changePasswordRequest.setOldPassword("oldPass");
        changePasswordRequest.setNewPassword("newPass");
    }

    @Test
    @DisplayName("Login should return 200 OK for valid credentials")
    void login_credentialsAreValid() {
        doNothing().when(authenticationService).authenticate(any(Credentials.class));

        ResponseEntity<Void> response = authController.login("test.user", "correctPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(authenticationService).authenticate(any(Credentials.class));
    }

    @Test
    @DisplayName("Login should return 401 Unauthorized for invalid credentials")
    void login_credentialsAreInvalid() {
        doThrow(new InvalidCredentialsException("Invalid credentials"))
                .when(authenticationService).authenticate(any(Credentials.class));

        ResponseEntity<Void> response = authController.login("test.user", "wrongPassword");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Change Password should return 200 OK when user is a Trainee")
    void changePassword_userIsTrainee() {
        doNothing().when(traineeService).changePassword(anyString(), anyString(), anyString());

        ResponseEntity<Void> response = authController.changePassword(changePasswordRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(traineeService).changePassword("test.user", "oldPass", "newPass");
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

        verify(traineeService).changePassword("test.user", "oldPass", "newPass");
        verify(trainerService).changePassword("test.user", "oldPass", "newPass");
    }

    @Test
    @DisplayName("Change Password should return 401 Unauthorized for incorrect old password")
    void changePassword_oldPasswordIsIncorrect() {
        doThrow(new SecurityException("Incorrect old password"))
                .when(traineeService).changePassword(anyString(), anyString(), anyString());
        doThrow(new SecurityException("Incorrect old password"))
                .when(trainerService).changePassword(anyString(), anyString(), anyString());

        ResponseEntity<Void> response = authController.changePassword(changePasswordRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
