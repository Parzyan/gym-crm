package com.company.gym.controller;

import com.company.gym.dto.request.TrainerRegistrationRequest;
import com.company.gym.dto.request.UpdateActiveStatusRequest;
import com.company.gym.dto.request.UpdateTrainerProfileRequest;
import com.company.gym.dto.response.TrainerProfileResponse;
import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.*;
import com.company.gym.exception.EntityNotFoundException;
import com.company.gym.service.TrainerService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private TrainerController trainerController;

    private Trainer testTrainer;
    private TrainerRegistrationRequest registrationRequest;
    private UpdateTrainerProfileRequest updateProfileRequest;
    private UpdateActiveStatusRequest statusRequest;

    @BeforeEach
    void setUp() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

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

        trainerController = new TrainerController(trainerService, meterRegistry);
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

        verify(trainerService, times(1)).createTrainerProfile(
                eq("Jane"),
                eq("Trainer"),
                eq(1L)
        );
    }

    @Test
    @DisplayName("Register Trainer should throw IllegalArgumentException on failure")
    void registerTrainer_onFailure() {
        when(trainerService.createTrainerProfile(anyString(), anyString(), anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid input data"));

        assertThrows(IllegalArgumentException.class, () -> trainerController.registerTrainer(registrationRequest));
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
    @DisplayName("Get Trainer Profile should throw EntityNotFoundException if trainer does not exist")
    void getTrainerProfile_trainerNotFound() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerController.getTrainerProfile("jane.trainer"));
    }

    @Test
    @DisplayName("Update Trainer Profile should return 200 OK on success")
    void updateTrainerProfile_onSuccess() {
        testTrainer.getUser().setIsActive(false);

        when(trainerService.getByUsername("jane.trainer"))
                .thenReturn(Optional.of(testTrainer))
                .thenReturn(Optional.of(testTrainer));

        ResponseEntity<TrainerProfileResponse> response = trainerController.updateTrainerProfile("jane.trainer", updateProfileRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().getFirstName());

        verify(trainerService, times(2)).getByUsername("jane.trainer");
        verify(trainerService, times(1)).updateStatus(
                argThat(credentials ->
                        credentials.getUsername().equals("jane.trainer") &&
                                credentials.getPassword() == null
                )
        );
    }

    @Test
    @DisplayName("Update Trainer Profile should throw EntityNotFoundException if trainer does not exist")
    void updateTrainerProfile_trainerNotFound() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerController.updateTrainerProfile("jane.trainer", updateProfileRequest));
    }

    @Test
    @DisplayName("Update Trainer Status should return 200 OK on success")
    void updateTrainerStatus_onSuccess() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.of(testTrainer));
        doNothing().when(trainerService).updateStatus(any(Credentials.class));

        ResponseEntity<Void> response = trainerController.updateTrainerStatus(statusRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainerService, times(1)).updateStatus(
                argThat(credentials ->
                        credentials.getUsername().equals("jane.trainer") &&
                                credentials.getPassword().equals("password123")
                )
        );
    }

    @Test
    @DisplayName("Update Trainer Status should throw EntityNotFoundException if trainer not found")
    void updateTrainerStatus_trainerNotFound() {
        when(trainerService.getByUsername("jane.trainer")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerController.updateTrainerStatus(statusRequest));
    }
}
