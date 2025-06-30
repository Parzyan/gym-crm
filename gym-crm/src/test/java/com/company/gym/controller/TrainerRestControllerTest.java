package com.company.gym.controller;

import com.company.gym.dto.request.TrainerRegistrationRequest;
import com.company.gym.dto.request.UpdateActiveStatusRequest;
import com.company.gym.dto.request.UpdateTrainerProfileRequest;
import com.company.gym.dto.response.TrainerProfileResponse;
import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.*;
import com.company.gym.exception.InvalidCredentialsException;
import com.company.gym.service.AuthenticationService;
import com.company.gym.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerRestControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private TrainerRestController trainerRestController;

    private Trainer testTrainer;
    private TrainerRegistrationRequest registrationRequest;
    private UpdateTrainerProfileRequest updateRequest;
    private UpdateActiveStatusRequest statusRequest;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setUsername("jane.trainer");
        testUser.setPassword("password123");
        testUser.setFirstName("Jane");
        testUser.setLastName("Trainer");
        testUser.setIsActive(true);

        TrainingType testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Yoga");

        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUser(testUser);
        testTrainer.setSpecialization(testTrainingType);

        Trainee testTrainee = new Trainee();
        User traineeUser = new User();
        traineeUser.setUsername("john.doe");
        traineeUser.setFirstName("John");
        traineeUser.setLastName("Doe");
        testTrainee.setUser(traineeUser);

        Training testTraining = new Training();
        testTraining.setTrainee(testTrainee);
        testTraining.setTrainer(testTrainer);
        testTrainer.setTrainings(Collections.singletonList(testTraining));

        registrationRequest = new TrainerRegistrationRequest();
        registrationRequest.setFirstName("Jane");
        registrationRequest.setLastName("Trainer");
        registrationRequest.setSpecializationId(1L);

        updateRequest = new UpdateTrainerProfileRequest();
        updateRequest.setUsername("jane.trainer");
        updateRequest.setPassword("password123");
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Trainer");
        updateRequest.setActive(true);

        statusRequest = new UpdateActiveStatusRequest();
        statusRequest.setUsername("jane.trainer");
        statusRequest.setPassword("password123");
        statusRequest.setActive(true);
    }

    @Test
    @DisplayName("Register Trainer should return 201 Created on success")
    void registerTrainer_onSuccess() {
        when(trainerService.createTrainerProfile(anyString(), anyString(), anyLong())).thenReturn(testTrainer);

        ResponseEntity<UserCredentialsResponse> response = trainerRestController.registerTrainer(registrationRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jane.trainer", response.getBody().getUsername());
    }

    @Test
    @DisplayName("Register Trainer should return 400 Bad Request on failure")
    void registerTrainer_onFailure() {
        when(trainerService.createTrainerProfile(anyString(), anyString(), anyLong())).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<UserCredentialsResponse> response = trainerRestController.registerTrainer(registrationRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Get Trainer Profile should return 200 OK when credentials are valid")
    void getTrainerProfile_whenCredentialsValid() {
        doNothing().when(authenticationService).authenticate(any(Credentials.class));
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.of(testTrainer));

        ResponseEntity<TrainerProfileResponse> response = trainerRestController.getTrainerProfile("jane.trainer", "correctPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().getFirstName());
    }

    @Test
    @DisplayName("Get Trainer Profile should return 401 Unauthorized when credentials are invalid")
    void getTrainerProfile_whenCredentialsInvalid() {
        doThrow(new InvalidCredentialsException("Invalid password"))
                .when(authenticationService).authenticate(any(Credentials.class));

        ResponseEntity<TrainerProfileResponse> response = trainerRestController.getTrainerProfile("jane.trainer", "wrongPassword");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(trainerService, never()).getByUsername(anyString());
    }

    @Test
    @DisplayName("Update Trainer Profile should return 200 OK on success")
    void updateTrainerProfile_onSuccess() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.of(testTrainer));

        ResponseEntity<TrainerProfileResponse> response = trainerRestController.updateTrainerProfile(updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().getFirstName());
        verify(trainerService, never()).updateStatus(any());
    }

    @Test
    @DisplayName("Update Trainer Profile should call updateStatus when isActive changes")
    void updateTrainerProfile_whenStatusChanges() {
        updateRequest.setActive(false);
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.of(testTrainer));

        trainerRestController.updateTrainerProfile(updateRequest);

        verify(trainerService, times(1)).updateStatus(any(Credentials.class));
    }

    @Test
    @DisplayName("Update Trainer Profile should return 404 Not Found if trainer does not exist")
    void updateTrainerProfile_whenTrainerNotFound_shouldReturnNotFound() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.empty());

        ResponseEntity<TrainerProfileResponse> response = trainerRestController.updateTrainerProfile(updateRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Update Trainer Profile should return 401 Unauthorized on auth failure")
    void updateTrainerProfile_whenAuthFails() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.of(testTrainer));
        updateRequest.setActive(false);
        doThrow(new InvalidCredentialsException("Wrong password")).when(trainerService).updateStatus(any(Credentials.class));

        ResponseEntity<TrainerProfileResponse> response = trainerRestController.updateTrainerProfile(updateRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Update Status should return 200 OK and call service when status changes")
    void updateTrainerStatus_whenStatusChanges() {
        statusRequest.setActive(false);
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.of(testTrainer));
        doNothing().when(trainerService).updateStatus(any(Credentials.class));

        ResponseEntity<Void> response = trainerRestController.updateTrainerStatus(statusRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainerService, times(1)).updateStatus(any(Credentials.class));
    }

    @Test
    @DisplayName("Update Status should return 200 OK and NOT call service when status is same")
    void updateTrainerStatus_whenStatusIsSame() {
        statusRequest.setActive(true);
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.of(testTrainer));

        ResponseEntity<Void> response = trainerRestController.updateTrainerStatus(statusRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainerService, never()).updateStatus(any(Credentials.class));
    }
}
