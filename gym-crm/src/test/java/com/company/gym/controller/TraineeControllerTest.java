package com.company.gym.controller;

import com.company.gym.dto.request.*;
import com.company.gym.dto.response.TraineeProfileResponse;
import com.company.gym.dto.response.UnassignedTrainerResponse;
import com.company.gym.dto.response.UpdatedTrainersListResponse;
import com.company.gym.dto.response.UserCredentialsResponse;
import com.company.gym.entity.*;
import com.company.gym.exception.EntityNotFoundException;
import com.company.gym.exception.InvalidInputException;
import com.company.gym.service.TraineeService;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TraineeController traineeController;

    private Trainee testTrainee;
    private TraineeRegistrationRequest registrationRequest;
    private UpdateTraineeProfileRequest updateRequest;
    private UpdateTrainerForTrainingRequest updateTrainerRequest;
    private UpdateActiveStatusRequest statusRequest;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john.doe");
        testUser.setPassword("password123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setIsActive(true);

        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUser(testUser);
        testTrainee.setAddress("123 Main St");
        testTrainee.setDateOfBirth(new Date());
        testTrainee.setTrainings(Collections.singletonList(getTraining()));

        registrationRequest = new TraineeRegistrationRequest();
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");
        registrationRequest.setDateOfBirth(new Date());
        registrationRequest.setAddress("123 Main St");

        updateRequest = new UpdateTraineeProfileRequest();
        updateRequest.setDateOfBirth(new Date());
        updateRequest.setAddress("456 Elm St");
        updateRequest.setActive(true);

        updateTrainerRequest = new UpdateTrainerForTrainingRequest();
        updateTrainerRequest.setTrainingId(1L);
        updateTrainerRequest.setNewTrainerId(2L);

        statusRequest = new UpdateActiveStatusRequest();
        statusRequest.setUsername("john.doe");
        statusRequest.setPassword("password123");
        statusRequest.setActive(false);
    }

    private Training getTraining() {
        TrainingType testTrainingType = new TrainingType();
        testTrainingType.setTrainingTypeName("Cardio");

        Trainer testTrainer = new Trainer();
        User trainerUser = new User();
        trainerUser.setUsername("jane.trainer");
        trainerUser.setFirstName("Jane");
        trainerUser.setLastName("Trainer");
        testTrainer.setUser(trainerUser);
        testTrainer.setSpecialization(testTrainingType);

        Training testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTrainee(testTrainee);
        testTraining.setTrainer(testTrainer);
        return testTraining;
    }

    @Test
    @DisplayName("Register Trainee should return 201 Created on success")
    void registerTrainee_onSuccess() {
        when(traineeService.createTraineeProfile(anyString(), anyString(), any(), anyString())).thenReturn(testTrainee);

        ResponseEntity<UserCredentialsResponse> response = traineeController.registerTrainee(registrationRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe", response.getBody().getUsername());
    }

    @Test
    @DisplayName("Register Trainee should return 400 Bad Request on failure")
    void registerTrainee_onFailure() {
        when(traineeService.createTraineeProfile(anyString(), anyString(), any(), anyString()))
                .thenThrow(new InvalidInputException("Invalid input data"));

        ResponseEntity<UserCredentialsResponse> response = traineeController.registerTrainee(registrationRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(traineeService, times(1)).createTraineeProfile(anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Get Trainee Profile should return 200 OK")
    void getTraineeProfile_onSuccess() {
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        ResponseEntity<TraineeProfileResponse> response = traineeController.getTraineeProfile("john.doe");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
    }

    @Test
    @DisplayName("Get Trainee Profile should return 404 Not Found if trainee does not exist")
    void getTraineeProfile_traineeNotFound() {
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.empty());

        ResponseEntity<TraineeProfileResponse> response = traineeController.getTraineeProfile("john.doe");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Update Trainee Profile should return 200 OK on success")
    void updateTraineeProfile_onSuccess() {
        when(traineeService.updateTraineeProfile(any(Credentials.class), any(), any())).thenReturn(testTrainee);
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        ResponseEntity<TraineeProfileResponse> response = traineeController.updateTraineeProfile("john.doe", updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
        verify(traineeService, never()).updateStatus(any());
    }

    @Test
    @DisplayName("Update Trainer for Training should return 200 OK on success")
    void updateTrainerForTraining_onSuccess() {
        when(traineeService.getTrainersForTrainee("john.doe")).thenReturn(Collections.emptyList());

        ResponseEntity<UpdatedTrainersListResponse> response = traineeController
                .updateTrainerForTraining("john.doe", updateTrainerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).updateTrainerForTraining(any(Credentials.class), anyLong(), anyLong());
    }

    @Test
    @DisplayName("Update Trainer for Training should return 404 Not Found if training does not exist")
    void updateTrainerForTraining_trainingNotFound() {
        doThrow(new EntityNotFoundException("Training not found"))
                .when(traineeService).updateTrainerForTraining(any(Credentials.class), anyLong(), anyLong());

        ResponseEntity<UpdatedTrainersListResponse> response = traineeController
                .updateTrainerForTraining("john.doe", updateTrainerRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Get Unassigned Trainers should return 200 OK with filtered list")
    void getUnassignedTrainers_onSuccess() {
        Trainer activeTrainer = new Trainer();
        User activeUser = new User();
        activeUser.setIsActive(true);
        activeTrainer.setUser(activeUser);
        activeTrainer.setSpecialization(new TrainingType());

        when(trainerService.getUnassignedTrainers("john.doe")).thenReturn(Collections.singletonList(activeTrainer));

        ResponseEntity<List<UnassignedTrainerResponse>> response = traineeController.getUnassignedTrainers("john.doe");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Activate Trainee should return 200 OK on success")
    void activateTrainee_onSuccess() {
        when(traineeService.getByUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        doNothing().when(traineeService).updateStatus(any(Credentials.class));

        ResponseEntity<Void> response = traineeController.updateTraineeStatus(statusRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService, times(1)).updateStatus(any(Credentials.class));
    }
}
