package com.company.gym.controller;

import com.company.gym.dto.request.TrainerRegistrationRequest;
import com.company.gym.dto.request.UpdateActiveStatusRequest;
import com.company.gym.dto.request.UpdateTrainerProfileRequest;
import com.company.gym.dto.response.TrainerProfileResponse;
import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.*;
import com.company.gym.exception.InvalidCredentialsException;
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
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TrainerController trainerController;

    private Trainer testTrainer;
    private TrainerRegistrationRequest registrationRequest;
    private UpdateTrainerProfileRequest updateProfileRequest;
    private UpdateActiveStatusRequest statusRequest;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("jane.trainer");
        testUser.setPassword("password123");
        testUser.setFirstName("Jane");
        testUser.setLastName("Trainer");
        testUser.setIsActive(true);

        TrainingType testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Cardio");

        testTrainer = new Trainer();
        testTrainer.setId(1L);
        testTrainer.setUser(testUser);
        testTrainer.setSpecialization(testTrainingType);
        testTrainer.setTrainings(Collections.singletonList(getTraining()));

        registrationRequest = new TrainerRegistrationRequest();
        registrationRequest.setFirstName("Jane");
        registrationRequest.setLastName("Trainer");
        registrationRequest.setSpecializationId(1L);

        updateProfileRequest = new UpdateTrainerProfileRequest();
        updateProfileRequest.setActive(true);

        statusRequest = new UpdateActiveStatusRequest();
        statusRequest.setUsername("jane.trainer");
        statusRequest.setPassword("password123");
        statusRequest.setActive(false);
    }

    private Training getTraining() {
        Trainee testTrainee = new Trainee();
        User traineeUser = new User();
        traineeUser.setUsername("john.doe");
        traineeUser.setFirstName("John");
        traineeUser.setLastName("Doe");
        testTrainee.setUser(traineeUser);

        Training testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTrainer(testTrainer);
        testTraining.setTrainee(testTrainee);
        return testTraining;
    }

    @Test
    @DisplayName("Register Trainer should return 201 Created on success")
    void registerTrainer_onSuccess() {
        when(trainerService.createTrainerProfile(anyString(), anyString(), anyLong())).thenReturn(testTrainer);

        ResponseEntity<UserCredentialsResponse> response = trainerController.registerTrainer(registrationRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jane.trainer", response.getBody().getUsername());
    }

    @Test
    @DisplayName("Register Trainer should return 400 Bad Request on failure")
    void registerTrainer_onFailure() {
        when(trainerService.createTrainerProfile(anyString(), anyString(), anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid input data"));

        ResponseEntity<UserCredentialsResponse> response = trainerController.registerTrainer(registrationRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(trainerService, times(1)).createTrainerProfile(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Get Trainer Profile should return 200 OK")
    void getTrainerProfile_onSuccess() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.of(testTrainer));

        ResponseEntity<TrainerProfileResponse> response = trainerController.getTrainerProfile("jane.trainer");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().getFirstName());
        assertEquals("Cardio", response.getBody().getSpecialization());
    }

    @Test
    @DisplayName("Get Trainer Profile should return 404 Not Found if trainer does not exist")
    void getTrainerProfile_trainerNotFound() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.empty());

        ResponseEntity<TrainerProfileResponse> response = trainerController.getTrainerProfile("jane.trainer");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Update Trainer Profile should return 200 OK on success")
    void updateTrainerProfile_onSuccess() {
        testTrainer.getUser().setIsActive(updateProfileRequest.isActive());

        when(trainerService.getByUsername("jane.trainer"))
                .thenReturn(Optional.of(testTrainer))
                .thenReturn(Optional.of(testTrainer));

        ResponseEntity<TrainerProfileResponse> response = trainerController.updateTrainerProfile("jane.trainer", updateProfileRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().getFirstName());

        verify(trainerService, times(2)).getByUsername("jane.trainer");
        verify(trainerService, never()).updateStatus(any());
    }

    @Test
    @DisplayName("Update Trainer Profile should return 404 Not Found if trainer does not exist")
    void updateTrainerProfile_trainerNotFound() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.empty());

        ResponseEntity<TrainerProfileResponse> response = trainerController.updateTrainerProfile("jane.trainer", updateProfileRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Update Trainer Status should return 200 OK on success")
    void updateTrainerStatus_onSuccess() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.of(testTrainer));
        doNothing().when(trainerService).updateStatus(any(Credentials.class));

        ResponseEntity<Void> response = trainerController.updateTrainerStatus(statusRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainerService, times(1)).updateStatus(any(Credentials.class));
    }

    @Test
    @DisplayName("Update Trainer Status should return 400 Bad Request if trainer not found")
    void updateTrainerStatus_trainerNotFound() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = trainerController.updateTrainerStatus(statusRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Update Trainer Status should return 401 Unauthorized on invalid credentials")
    void updateTrainerStatus_invalidCredentials() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.of(testTrainer));
        doThrow(new InvalidCredentialsException("Invalid credentials"))
                .when(trainerService).updateStatus(any(Credentials.class));

        ResponseEntity<Void> response = trainerController.updateTrainerStatus(statusRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
